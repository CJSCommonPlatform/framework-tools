package uk.gov.justice.framework.tools.replay;

import static org.apache.commons.lang3.StringUtils.substringBefore;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class AsyncStreamDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncStreamDispatcher.class);

    @Inject
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Inject
    private JdbcEventRepository jdbcEventRepository;

    public UUID dispatch(final UUID streamId) {

        try(final Stream<JsonEnvelope> stream = jdbcEventRepository.getByStreamId(streamId)) {

            final int[] noOfProcessedElements = {0};
            final JsonEnvelope[] envelopes = {null};

                stream.forEach(envelope -> {
                    if (firstElement(noOfProcessedElements)) {
                        LOGGER.info("Starting processing of stream: {}", streamIdOf(envelope));
                    }
                    try {
                        envelopeDispatcher.dispatch(envelope);
                    } catch (MissingHandlerException ex) {
                        final Metadata metadata = envelope.metadata();
                        LOGGER.warn("Missing handler for stream Id: {}, event name: {}, version: {}", metadata.streamId().get(),
                                metadata.name(), metadata.version().get());
                    }
                    noOfProcessedElements[0]++;
                    if (shouldLogProgress(noOfProcessedElements)) {
                        LOGGER.info("Processed {} elements of stream: {}", noOfProcessedElements[0], streamIdOf(envelope));
                    }
                    envelopes[0] = envelope;
                });
            doInsert(new StreamStatus(streamId, versionOf(envelopes[0]), sourceOf(envelopes[0])));
            LOGGER.info("Finished processing of stream: {}, elements processed: {}", streamId, noOfProcessedElements[0]);
                return streamId;

        }
    }

    @Transactional
    private void doInsert(final StreamStatus streamStatus) {
        streamStatusRepository.insert(streamStatus);
    }

    private UUID streamIdOf(final JsonEnvelope envelope) {
        return envelope
                .metadata()
                .streamId()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Stream id not found in the envelope: %s", envelope.toString())));
    }

    private Long versionOf(final JsonEnvelope envelope) {
        return envelope
                .metadata()
                .version()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Version not found in the envelope: %s", envelope.toString())));
    }

    private String sourceOf(final JsonEnvelope envelope) {
        return substringBefore(envelope.metadata().name(), ".");
    }

    private boolean shouldLogProgress(final int[] i) {
        return i[0] % 100 == 0;
    }

    private boolean firstElement(final int[] i) {
        return i[0] == 0;
    }
}
