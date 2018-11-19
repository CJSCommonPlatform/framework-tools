package uk.gov.justice.framework.tools.replay.database;

import static java.lang.String.format;

import uk.gov.justice.framework.tools.replay.events.User;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

public class EventInserter {

    private final EventStreamJdbcRepository eventStreamJdbcRepository;
    private final EventJdbcRepository eventJdbcRepository;
    private final DataSource viewStoreDataSource;

    public EventInserter(final DataSource eventStoreDataSource, final DataSource viewStoreDataSource) {
        this.viewStoreDataSource = viewStoreDataSource;
        this.eventStreamJdbcRepository = new EventStreamJdbcRepositoryFactory().create(eventStoreDataSource);
        this.eventJdbcRepository = new EventJdbcRepositoryFactory().create(eventStoreDataSource);
    }

    @SuppressWarnings("SameParameterValue")
    public void insertEventsIntoVewstore(final UUID streamId, final List<Event> insertedEvents) {
        eventStreamJdbcRepository.insert(streamId);
        insertedEvents.forEach(this::insertIntoViewStore);
    }

    public List<User> getUsersFromViewStore() throws SQLException {
        final List<User> users = new ArrayList<>();
        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM users");
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final UUID userId = UUID.fromString(rs.getString("user_id"));
                final String firstName = rs.getString("first_name");
                final String lastName = rs.getString("last_name");
                users.add(new User(userId, firstName, lastName));
            }

            return users;
        }
    }

    public Stream<Event> getAllFromEventStore() {
        return eventJdbcRepository.findAll();
    }

    private void insertIntoViewStore(final Event event) {
        try {
            eventJdbcRepository.insert(event);
        } catch (final InvalidPositionException e) {
            throw new RuntimeException(format("Failed to insert Event '%s", event), e);
        }
    }
}
