package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import uk.gov.justice.framework.tools.replay.database.DatasourceCreator;
import uk.gov.justice.framework.tools.replay.database.EventInserter;
import uk.gov.justice.framework.tools.replay.database.LiquibaseRunner;
import uk.gov.justice.framework.tools.replay.events.User;
import uk.gov.justice.framework.tools.replay.events.UserFactory;
import uk.gov.justice.framework.tools.replay.h2.InMemoryDatabaseRunner;
import uk.gov.justice.framework.tools.replay.wildfly.WildflyRunner;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InsertAllEventsIntoViewStoreIT {

    private static final Boolean SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE = true;
    private static final Boolean ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY = false;

    private static final int WILDFLY_TIMEOUT_IN_SECONDS = 60;
    private static final int NUMBER_OF_EVENTS_TO_INSERT = 100;

    private final LiquibaseRunner liquibaseRunner = new LiquibaseRunner();
    private final DatasourceCreator datasourceCreator = new DatasourceCreator();
    private final WildflyRunner wildflyRunner = new WildflyRunner();

    private final DataSource viewStoreDataSource = datasourceCreator.createViewStoreDataSource();
    private final DataSource eventStoreDataSource = datasourceCreator.createEventStoreDataSource();
    private final EventInserter eventInserter = new EventInserter(eventStoreDataSource, viewStoreDataSource);
    private final UserFactory userFactory = new UserFactory();
    private final InMemoryDatabaseRunner inMemoryDatabaseRunner = new InMemoryDatabaseRunner();

    @Before
    public void startDatabase() {
        inMemoryDatabaseRunner.startH2Database();
    }

    @Before
    public void runLiquibase() throws Exception {
        liquibaseRunner.createEventStoreSchema(eventStoreDataSource);
        liquibaseRunner.createViewStoreSchema(viewStoreDataSource);
    }

    @After
    public void stopDB() throws Exception {
        inMemoryDatabaseRunner.stopH2Database();
    }

    @Test
    public void shouldReadEventsFromEventStoreAndInsertIntoViewStore() throws Exception {

        System.out.println(format("Inserting %d events with timeout of %d seconds", NUMBER_OF_EVENTS_TO_INSERT, WILDFLY_TIMEOUT_IN_SECONDS));

        final UUID streamId = randomUUID();
        final String eventName = "framework.update-user";

        final List<User> users = userFactory.createSomeUsers(NUMBER_OF_EVENTS_TO_INSERT);
        final List<Event> someEvents = userFactory.convertToEvents(users, eventName, streamId);

        eventInserter.insertEventsIntoVewstore(
                streamId,
                someEvents);

        final List<Event> insertedEvents = eventInserter.getAllFromEventStore().collect(toList());

        assertThat(insertedEvents.size(), is(NUMBER_OF_EVENTS_TO_INSERT));

        System.out.println(format("%d events inserted into view store", insertedEvents.size()));

        final boolean wildflyRanSuccessfully = wildflyRunner.run(
                WILDFLY_TIMEOUT_IN_SECONDS,
                SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE,
                ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY
        );

        assertTrue("Wildfly process exited abnormally", wildflyRanSuccessfully);

        final List<User> usersFromViewStore = eventInserter.getUsersFromViewStore();


        System.out.println(usersFromViewStore.size() + " events of " + NUMBER_OF_EVENTS_TO_INSERT + " were replayed into the view store");

        assertThat(usersFromViewStore.size(), is(NUMBER_OF_EVENTS_TO_INSERT));

        users.forEach(usersFromViewStore::remove);

        assertTrue(usersFromViewStore.isEmpty());
    }
}
