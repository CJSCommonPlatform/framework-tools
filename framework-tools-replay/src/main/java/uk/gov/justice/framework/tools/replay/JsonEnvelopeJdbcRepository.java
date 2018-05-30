package uk.gov.justice.framework.tools.replay;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class JsonEnvelopeJdbcRepository {

    private static final long PAGE_SIZE_OF_ONE = 1L;
    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Inject
    private EventConverter eventConverter;

    public Stream<JsonEnvelope> forward(final UUID streamId, final long position, final long pageSize) {
        return eventJdbcRepository
                .forward(streamId, position, pageSize)
                .map(eventConverter::envelopeOf);
    }

    public JsonEnvelope head(final UUID streamId) {
        return eventJdbcRepository.head(streamId, PAGE_SIZE_OF_ONE)
                .findFirst()
                .map(eventConverter::envelopeOf)
                .orElseThrow(() -> new RuntimeException(format("Failed to get head for stream id: %s", streamId)));
    }

    public long getLatestSequenceIdForStream(final UUID streamId) {
        return eventJdbcRepository.getLatestSequenceIdForStream(streamId);
    }
}
