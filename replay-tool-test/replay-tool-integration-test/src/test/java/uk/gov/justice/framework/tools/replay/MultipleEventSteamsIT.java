package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

import uk.gov.justice.framework.tools.replay.database.DatasourceCreator;
import uk.gov.justice.framework.tools.replay.database.EventInserter;
import uk.gov.justice.framework.tools.replay.database.LiquibaseRunner;
import uk.gov.justice.framework.tools.replay.events.User;
import uk.gov.justice.framework.tools.replay.events.UserFactory;
import uk.gov.justice.framework.tools.replay.wildfly.WildflyRunner;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class MultipleEventSteamsIT {

    private static final Boolean SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE = true;
    private static final Boolean ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY = false;

    private static final int SECONDS_IN_A_MINUTE = 60;

    private static final int WILDFLY_TIMEOUT_IN_SECONDS = SECONDS_IN_A_MINUTE * 5;
    private static final int NUMBER_OF_EVENTS_TO_INSERT = 100;
    private static final int NUMBER_OF_STREAMS = 10;

    private final LiquibaseRunner liquibaseRunner = new LiquibaseRunner();
    private final DatasourceCreator datasourceCreator = new DatasourceCreator();
    private final WildflyRunner wildflyRunner = new WildflyRunner();

    private final DataSource viewStoreDataSource = datasourceCreator.createViewStoreDataSource();
    private final DataSource eventStoreDataSource = datasourceCreator.createEventStoreDataSource();
    private final EventInserter eventInserter = new EventInserter(eventStoreDataSource, viewStoreDataSource);
    private final UserFactory userFactory = new UserFactory();


    @Before
    public void runLiquibase() throws Exception {
        liquibaseRunner.createEventStoreSchema(eventStoreDataSource);
        liquibaseRunner.createViewStoreSchema(viewStoreDataSource);
    }

    @Test
    public void shouldInsertEventsIntoMultipleStreams() throws Exception {

        final String eventName = "framework.update-user";

        final List<User> allInsertedUsers = new ArrayList<>();

        for(int i = 0; i < NUMBER_OF_STREAMS; i++) {
            final UUID streamId = randomUUID();
            System.out.println(format("Inserting %d events into stream %s", NUMBER_OF_EVENTS_TO_INSERT, streamId));
            final List<User> users = userFactory.createSomeUsers(NUMBER_OF_EVENTS_TO_INSERT);
            final List<Event> someEvents = userFactory.convertToEvents(users, eventName, streamId);

            eventInserter.insertEventsIntoVewstore(
                    streamId,
                    someEvents);

            allInsertedUsers.addAll(users);
        }

        final List<Event> insertedEvents = eventInserter.getAllFromEventStore().collect(toList());

        System.out.println(format("%d events inserted into view store in %d streams", insertedEvents.size(), NUMBER_OF_STREAMS));

        final boolean wildflyRanSuccessfully = wildflyRunner.run(
                WILDFLY_TIMEOUT_IN_SECONDS,
                SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE,
                ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY
        );

        assertTrue("Wildfly process exited abnormally", wildflyRanSuccessfully);

        final List<User> usersFromViewStore = eventInserter.getUsersFromViewStore();

        allInsertedUsers.forEach(usersFromViewStore::remove);

        assertTrue(usersFromViewStore.isEmpty());
    }
}
