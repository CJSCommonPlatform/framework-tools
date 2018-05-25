package uk.gov.justice.framework.tools.replay;


import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.rangeClosed;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.initEventStoreDb;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class TestEventRepository extends EventJdbcRepository {

    private final DataSource datasource;

    public TestEventRepository() throws Exception {
        this.datasource = initEventStoreDb();
        setField(this, "eventInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
        setField(this, "dataSource", datasource);
        setField(this, "jdbcRepositoryHelper", new JdbcRepositoryHelper());
    }

    public TestEventRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        this.datasource = dataSource;
    }

    public DataSource getDataSource() {
        return datasource;
    }

    public List<String> insertEventData(final UUID streamId, final int numberOfEventsToInsert) {
        return rangeClosed(1L, numberOfEventsToInsert)
                .mapToObj(sequenceId -> insertEvent(streamId, sequenceId))
                .collect(toList());
    }

    private String insertEvent(final UUID streamId, final long sequenceId) {
        final Event event = eventFrom("framework.example-test", streamId, sequenceId);


        try {
            insert(event);
        } catch (final InvalidSequenceIdException e) {
            throw new RuntimeException(e);
        }

        return event.getId().toString();
    }

    private Event eventFrom(final String eventName, final UUID eventStreamId, final long sequenceId) {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(ZonedDateTime.now())
                        .withVersion(sequenceId)
                        .withStreamId(eventStreamId).withVersion(1L))
                .withPayloadOf("test", "a string")
                .build();

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID id = metadata.id();

        final UUID streamId = metadata.streamId().orElse(null);
        final String name = metadata.name();
        final String payload = jsonEnvelope.payloadAsJsonObject().toString();
        final ZonedDateTime createdAt = metadata.createdAt().orElse(null);

        return new Event(id, streamId, sequenceId, name, metadata.asJsonObject().toString(), payload, createdAt);
    }
}
