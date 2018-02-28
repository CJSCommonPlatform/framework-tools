package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.initEventStoreDb;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
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

    public List<String> insertEventData(final UUID streamId) {

        final UUID documentId = UUID.randomUUID();

        List<String> collect = range(1L, 2L)
                .mapToObj(sequenceId -> insertEvent(streamId, sequenceId, documentId))
                .collect(toList());

        collect.addAll(range(2L, 3L)
                .mapToObj(sequenceId -> insertUpdateEvent(streamId, sequenceId, documentId))
                .collect(toList()));

        return collect;
    }

    private String insertUpdateEvent(final UUID streamId, final long sequenceId, final UUID documentId) {
        final Event secondEvent = updateEventFrom("framework.example.update", streamId, sequenceId, documentId);

        try {
            insert(secondEvent);
        } catch (final InvalidSequenceIdException e) {
            throw new RuntimeException(e);
        }

        return secondEvent.getId().toString();

    }
    private String insertEvent(final UUID streamId, final long sequenceId, final UUID documentId) {

        final Event event = eventFrom("framework.example-test", streamId, sequenceId, documentId);

        System.out.println(format("Inserting event-stream with id %s", event.getId()));

        try {
            insert(event);
        } catch (final InvalidSequenceIdException e) {
            throw new RuntimeException(e);
        }

        return event.getId().toString();
    }

    private Event updateEventFrom(final String eventName, final UUID eventStreamId, final long sequenceId, final UUID documentId) {
        final ZonedDateTime createdAt = new UtcClock().now();
        final Metadata metadata = metadataWithRandomUUID(eventName)
                .createdAt(createdAt)
                .withVersion(sequenceId)
                .withStreamId(eventStreamId).build();

        final JsonObject payload = createObjectBuilder().add("testId", eventStreamId.toString())
                .add("documentId", documentId.toString())
                .add("name", "newDocumentName").build();

        return new Event(metadata.id(), eventStreamId, sequenceId, eventName, metadata.asJsonObject().toString(), payload.toString(), createdAt);
    }

    private Event eventFrom(final String eventName, final UUID eventStreamId, final long sequenceId, final UUID documentId) {

        final JsonArray documentsArray = Json.createArrayBuilder()
                .add(createObjectBuilder()
                        .add("documentId", documentId.toString())
                        .add("name", "documentName").build())
                .add(createObjectBuilder()
                        .add("documentId", UUID.randomUUID().toString())
                        .add("name", "documentName2").build())
                .add(createObjectBuilder()
                        .add("documentId", UUID.randomUUID().toString())
                        .add("name", "documentName3").build()).build();


        final JsonObject payload = createObjectBuilder()
                .add("testId", eventStreamId.toString())
                .add("data", "a string")
                .add("documents", documentsArray).build();


        final ZonedDateTime createdAt = new UtcClock().now();
        final Metadata metadata = metadataWithRandomUUID(eventName)
                .createdAt(createdAt)
                .withVersion(sequenceId)
                .withStreamId(eventStreamId).build();

        return new Event(metadata.id(), eventStreamId, sequenceId, eventName, metadata.asJsonObject().toString(), payload.toString(), createdAt);
    }
}
