package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.cleanupDataSource;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.getDocuments;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.getDocumentsCount;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.getTestCount;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.getTestEntitiesFrom;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.initViewStoreDb;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.viewStoreEvents;

import uk.gov.justice.framework.tools.entity.Document;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Note, you need to run this test from mvn as their are additional maven steps
 */
public class ReplayIntegrationIT {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");
    private static final String PROCESS_FILE_LOCATION = TEST_PROPERTIES.value("process.file.location");
    private static final String EXECUTION_TIMEOUT = TEST_PROPERTIES.value("replay.execution.timeout");

    private static TestEventRepository EVENT_LOG_REPOSITORY;
    private static TestEventStreamJdbcRepository EVENT_STREAM_JDBC_REPOSITORY;

    private static DataSource viewStoreDataSource;

    @Before
    public void setUpDB() throws Exception {
        EVENT_LOG_REPOSITORY = new TestEventRepository();
        EVENT_STREAM_JDBC_REPOSITORY = new TestEventStreamJdbcRepository(EVENT_LOG_REPOSITORY.getDataSource());
        viewStoreDataSource = initViewStoreDb();
        createProcessFile();
    }

    @After
    public void tearDown() throws SQLException {
        cleanupDataSource(EVENT_LOG_REPOSITORY.getDataSource(), "event_log");
        cleanupDataSource(viewStoreDataSource, "document");
        cleanupDataSource(viewStoreDataSource, "test");
    }

    @Test
    public void runReplayTool() throws Exception {
        final UUID testId = randomUUID();
        insertEventStreamFor(testId);
        
        final List<String> insertedEvents = insertCreateAndUpdateDocumentEvents(testId, "newDocumentName");
        assertThat(insertedEvents.size(), is(2));

        runCommand(createCommandToExecuteReplay());

        assertThat(getTestCount(viewStoreDataSource), is(1));
        assertThat(getDocumentsCount(viewStoreDataSource), is(3));

        final List<uk.gov.justice.framework.tools.entity.Test> testEntities = getTestEntitiesFrom(viewStoreDataSource);
        assertThat(testEntities.size(), is(1));

        final uk.gov.justice.framework.tools.entity.Test testEntity = testEntities.get(0);
        assertThat(testEntity.getData(), is("a string"));
        assertThat(testEntity.getTestId(), is(testId));

        final List<Document> documents = getDocuments(viewStoreDataSource);
        assertThat(documents.size(), is(3));

        assertThat(documents.stream()
                .filter(d -> d.getName().equalsIgnoreCase("newDocumentName"))
                .count(), is(1L));

        assertThat(documents.stream()
                .filter(d -> d.getTestId().equals(testId))
                .count(), is(3L));
    }

    @Test
    public void shouldProcessMultipleCreateEvents() throws Exception {
        final UUID testId = randomUUID();
        insertEventStreamFor(testId);

        final List<String> insertedEvents = insertMultipleCreateEvents(testId);
        runCommand(createCommandToExecuteReplay());
        viewStoreEvents(viewStoreDataSource).forEach(viewStoreEvent -> {
            System.out.println(format("viewStoreEvent with id %s", viewStoreEvent));
            insertedEvents.remove(viewStoreEvent);
        });
    }

    private void insertEventStreamFor(final UUID streamId) {
        EVENT_STREAM_JDBC_REPOSITORY.insert(streamId);
    }

    private List<String> insertCreateAndUpdateDocumentEvents(final UUID streamId, final String updateDocumentName) {
        return EVENT_LOG_REPOSITORY.insertEventData(streamId, updateDocumentName);
    }

    private List<String> insertMultipleCreateEvents(final UUID streamId) {
        return EVENT_LOG_REPOSITORY.insertEventData(streamId);
    }


    private void runCommand(final String command) throws Exception {
        final Process exec = Runtime.getRuntime().exec(command);

        System.out.println("Process started, waiting for completion..");

        // Kill the process if timeout exceeded
        boolean processTerminated = exec.waitFor(Long.parseLong(EXECUTION_TIMEOUT), TimeUnit.SECONDS);

        if (!processTerminated) {
            System.err.println(format("WildFly Swarm process failed to terminate after %s seconds!", EXECUTION_TIMEOUT));
            Process terminating = exec.destroyForcibly();

            processTerminated = terminating.waitFor(10L, TimeUnit.SECONDS);
            if (!processTerminated) {
                System.err.println("Failed to forcibly terminate WildFly Swarm process!");
            } else {
                System.err.println("WildFly Swarm process forcibly terminated.");
            }
        } else {
            System.out.println("WildFly Swarm process terminated by Test.");
        }

    }

    private void createProcessFile() throws Exception {
        Runtime.getRuntime().exec(format("touch %s", PROCESS_FILE_LOCATION));
    }

    private String createCommandToExecuteReplay() {
        final String replayJarLocation = getResource("framework-tools-replay*.jar");
        final String standaloneDSLocation = getResource("standalone-ds.xml");
        final String listenerLocation = getResource("replay-tool-it-example-listener*.war");

        String debug = "";

        if (TEST_PROPERTIES.value("swarm.debug.enabled").equals("true")) {
            debug = TEST_PROPERTIES.value("swarm.debug.args");
        }

        return commandFrom(debug, replayJarLocation, standaloneDSLocation, listenerLocation);
    }

    private String commandFrom(final String debugString,
                               final String replayJarLocation,
                               final String standaloneDSLocation,
                               final String listenerLocation) {
        return format("java %s -Dorg.wildfly.swarm.mainProcessFile=%s -Devent.listener.war=%s -jar %s -c %s",
                debugString,
                PROCESS_FILE_LOCATION,
                listenerLocation,
                replayJarLocation,
                standaloneDSLocation);
    }

    private String getResource(final String pattern) {
        final File dir = new File(this.getClass().getClassLoader().getResource("").getPath());
        final FileFilter fileFilter = new WildcardFileFilter(pattern);
        return dir.listFiles(fileFilter)[0].getAbsolutePath();
    }
}
