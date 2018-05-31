package uk.gov.justice.framework.tools.replay;


import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
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

public class EventOrderingIT {

    private static final Boolean SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE = true;
    private static final Boolean ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY = false;

    private static final int WILDFLY_TIMEOUT_IN_SECONDS = 60;

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
    public void shouldInsertEventsInTheCorrectOrder() throws Exception {

        final UUID streamId = randomUUID();
        final String eventName = "framework.update-user";

        final UUID userId = randomUUID();


        final User user = new User(userId, "Fred", "Bloggs");
        final User updatedUser = new User(userId, "Billy", "Bloggs");


        final List<Event> someEvents = userFactory.convertToEvents(asList(user, updatedUser), eventName, streamId);

        eventInserter.insertEventsIntoVewstore(
                streamId,
                someEvents);

        final boolean wildflyRanSuccessfully = wildflyRunner.run(
                WILDFLY_TIMEOUT_IN_SECONDS,
                SHOULD_LOG_WILDFLY_PROCESS_TO_CONSOLE,
                ENABLE_REMOTE_DEBUGGING_FOR_WILDFLY
        );

        assertTrue("Wildfly process exited abnormally", wildflyRanSuccessfully);

        final List<User> usersFromViewStore = eventInserter.getUsersFromViewStore();

        assertThat(usersFromViewStore.size(), is(1));

        assertThat(usersFromViewStore.get(0).getUserId(), is(userId));
        assertThat(usersFromViewStore.get(0).getFirstName(), is("Billy"));
        assertThat(usersFromViewStore.get(0).getLastName(), is("Bloggs"));
    }
}
