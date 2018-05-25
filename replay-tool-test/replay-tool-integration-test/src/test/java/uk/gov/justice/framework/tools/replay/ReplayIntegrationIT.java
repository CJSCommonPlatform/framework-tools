package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.cleanupDataSource;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.initViewStoreDb;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.viewStoreEvents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReplayIntegrationIT {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");
    private static final String PROCESS_FILE_LOCATION = TEST_PROPERTIES.value("process.file.location");

    private static final int EXECUTION_TIMEOUT_IN_SECONDS = 60 * 10;
    private static final int NUMBER_OF_EVENTS_TO_INSERT = 500;

    private static TestEventRepository EVENT_LOG_REPOSITORY;
    private static TestEventStreamJdbcRepository EVENT_STREAM_JDBC_REPOSITORY;

    private static DataSource viewStoreDataSource;

    private static final Boolean SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE = false;


    @Before
    public void setUpDB() throws Exception {
        EVENT_LOG_REPOSITORY = new TestEventRepository();
        EVENT_STREAM_JDBC_REPOSITORY = new TestEventStreamJdbcRepository(EVENT_LOG_REPOSITORY.getDataSource());
        viewStoreDataSource = initViewStoreDb();
        createProcessFile();
    }

    @Test
    public void runReplayTool() throws Exception {

        System.out.println(format("Inserting %d events with timeout of %d seconds", NUMBER_OF_EVENTS_TO_INSERT, EXECUTION_TIMEOUT_IN_SECONDS));

        final List<String> insertedEvents = new ArrayList<>(insertEventData(randomUUID(), NUMBER_OF_EVENTS_TO_INSERT));

        System.out.println(format("%d events inserted", NUMBER_OF_EVENTS_TO_INSERT));

        runCommand(createCommandToExecuteReplay());
        final List<String> events = viewStoreEvents(viewStoreDataSource);

        System.out.println(events.size() + " events of " + NUMBER_OF_EVENTS_TO_INSERT + " were inserted into the view store");

        events.forEach(insertedEvents::remove);
        assertTrue(insertedEvents.isEmpty());
    }

    private List<String> insertEventData(final UUID streamId, final int numberOfEventsToInsert) {
        EVENT_STREAM_JDBC_REPOSITORY.insert(streamId);
        return EVENT_LOG_REPOSITORY.insertEventData(streamId, numberOfEventsToInsert);
    }

    @After
    public void tearDown() throws SQLException {
        cleanupDataSource(EVENT_LOG_REPOSITORY.getDataSource(), "event_log");
        cleanupDataSource(viewStoreDataSource, "test");
    }

    private void runCommand(final String command) throws Exception {
        final Process exec = Runtime.getRuntime().exec(command);

        System.out.println("Process started, waiting for completion..");


        if(SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE) {
            logWildflyProcessToConsole(exec);
        }

        // Kill the process if timeout exceeded
        boolean processTerminated = exec.waitFor(EXECUTION_TIMEOUT_IN_SECONDS, SECONDS);

        if (!processTerminated) {
            System.err.println(format("WildFly Swarm process failed to terminate after %s seconds!", EXECUTION_TIMEOUT_IN_SECONDS));
            Process terminating = exec.destroyForcibly();

            processTerminated = terminating.waitFor(10L, SECONDS);
            if (!processTerminated) {
                System.err.println("Failed to forcibly terminate WildFly Swarm process!");
            } else {
                System.err.println("WildFly Swarm process forcibly terminated.");
            }
        } else {
            System.out.println("WildFly Swarm process terminated by Test.");
        }
    }

    private void sendOutputOfWildflyToTerminal(final Process exec) {
        new Thread(() -> {
            try {
                final InputStream inputStream = exec.getInputStream();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ( (line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (final IOException e) {
                throw new RuntimeException("Error getting output of external process", e);
            }
        }).start();
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
        return format("java %s -Dorg.wildfly.swarm.mainProcessFile=%s -Devent.listener.war=%s -jar %s -c %s -Dswarm.logging=DEBUG",
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

    private void logWildflyProcessToConsole(final Process exec) {
        new Thread(() -> {
            try {
                final InputStream inputStream = exec.getInputStream();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ( (line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("Ooops", e);
            }
        }).start();
    }

}
