package uk.gov.justice.framework.tools.replay;


import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReplayIntegrationIT {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");

    private static final int EVENT_COUNT = 5;

    private static final UUID STREAM_ID = randomUUID();

    private static TestEventLogRepository EVENT_LOG_REPOSITORY;

    private static DataSource viewStoreDataSource;

    @Before
    public void setUpDB() throws Exception {
        EVENT_LOG_REPOSITORY = new TestEventLogRepository(initEventStoreDb());
        viewStoreDataSource = initViewStoreDb();
    }

    @Test
    public void runReplayTool() throws Exception {
        insertEventLogData();
        runCommand(createCommandToExecuteReplay());
        assertTrue(viewStoreEventsPresent());
    }

    public boolean viewStoreEventsPresent() throws SQLException {

        boolean rc = false;

        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM test")) {

            int count = 0;
            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                count++;
            }

            if (count == EVENT_COUNT) {
                rc = true;
            }
        }

        return rc;
    }


    @After
    public void tearDown() throws SQLException {

        final PreparedStatement preparedStatement = EVENT_LOG_REPOSITORY.getDataSource().getConnection().prepareStatement("delete from event_log");
        preparedStatement.executeUpdate();
        EVENT_LOG_REPOSITORY.getDataSource().getConnection().close();

        final PreparedStatement viewStorePreparedStatement = viewStoreDataSource.getConnection().prepareStatement("delete from test");
        viewStorePreparedStatement.executeUpdate();
        viewStorePreparedStatement.getConnection().close();
    }

    private static DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url",
                       "db.eventstore.userName",
                        "db.eventstore.password",
                        "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    private static DataSource initViewStoreDb() throws Exception {
        return initDatabase("db.viewstore.url",
                "db.eventstore.userName",
                "db.eventstore.password",
                "liquibase/viewstore-db-changelog.xml", "liquibase/event-buffer-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    private EventLog eventLogFrom(final String eventName, final Long sequenceId) {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(ZonedDateTime.now())
                        .withVersion(sequenceId)
                        .withStreamId(STREAM_ID).withVersion(1L))
                .withPayloadOf("test", "a string")
                .build();

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID id = metadata.id();

        final UUID streamId = metadata.streamId().get();
        final String name = metadata.name();
        final String payload = jsonEnvelope.payloadAsJsonObject().toString();
        final ZonedDateTime createdAt = metadata.createdAt().get();

        return new EventLog(id, streamId, sequenceId, name, metadata.asJsonObject().toString(), payload, createdAt);
    }


    private String createCommandToExecuteReplay() {

        final String replayJarLocation = getResource("framework-tools-replay*.jar");
        final String standaloneDSLocation = getResource("standalone-ds.xml");
        final String listenerLocation = getResource("framework-tools-test-listener*.war");

        String debug = "";

        if (TEST_PROPERTIES.value("swarm.debug.enabled").equals("true")) {
            debug = TEST_PROPERTIES.value("swarm.debug.args");
        }

        final String command = commandFrom(debug, replayJarLocation, standaloneDSLocation, listenerLocation);

        return command;
    }

    private String commandFrom(final String debugString,
                               final String replayJarLocation,
                               final String standaloneDSLocation,
                               final String listenerLocation) {
        return String.format("java %s -Devent.listener.war=%s -jar %s -c %s",
                debugString,
                listenerLocation,
                replayJarLocation,
                standaloneDSLocation);
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(TEST_PROPERTIES.value("db.driver"));

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }

    private String getResource(final String pattern) {
        final File dir = new File(this.getClass().getClassLoader().getResource("").getPath());
        final FileFilter fileFilter = new WildcardFileFilter(pattern);
        return dir.listFiles(fileFilter)[0].getAbsolutePath();
    }

    public void runCommand(final String command) throws Exception {

        final Process exec = Runtime.getRuntime().exec(command);

        new Thread(() -> {
            System.out.println("Redirecting output...");
            try (final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(exec.getInputStream()))) {

                final Pattern p = Pattern.compile(".*WFSWARM99999: WildFly Swarm is Ready.*", Pattern.MULTILINE | Pattern.DOTALL);
                String line = "";
                while ((line = reader.readLine()) != null) {

                    System.out.println(line);

                    if (p.matcher(line).matches()) {
                        // Fraction has run so kill server now
                        exec.destroyForcibly();
                        break;
                    }

                }
            }
            catch (IOException ioEx) {
                System.out.println("IOException occurred reading process input stream");
            }

            }).start();

        System.out.println("Process started, waiting for completion..");

        // Give the process 60 seconds to complete and then kill it. Successful test will be
        // determined by querying the ViewStore for associated records later. The above Thread should
        // kill the process inside 60 seconds but wait here and handle shutdown if things take
        // too long for some reason
        boolean processTerminated = exec.waitFor(60L, TimeUnit.SECONDS);

        if (!processTerminated) {
            System.err.println("WildFly Swarm process failed to terminate after 60 seconds!");
            Process terminating = exec.destroyForcibly();

            processTerminated = terminating.waitFor(10L, TimeUnit.SECONDS);
            if (!processTerminated) {
                System.err.println("Failed to forcibly terminate WildFly Swarm process!");
            }
            else {
                System.err.println("WildFly Swarm process forcibly terminated.");
            }
        }
        else {
            System.out.println("WildFly Swarm process terminated by Test.");
        }

    }

    private void insertEventLogData() throws SQLException, InvalidSequenceIdException {
        Long sequenceId = 0L;
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test", ++sequenceId));
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test", ++sequenceId));
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test", ++sequenceId));
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test", ++sequenceId));
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test", ++sequenceId));
    }
}
