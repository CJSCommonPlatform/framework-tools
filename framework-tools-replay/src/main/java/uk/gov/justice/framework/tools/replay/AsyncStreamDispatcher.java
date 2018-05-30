package uk.gov.justice.framework.tools.replay;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

@Stateless
public class AsyncStreamDispatcher {

    private static final int PAGE_SIZE = 1000;
    private static final long FIRST_POSITION = 1L;

    @Inject
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Inject
    private StreamStatusFactory streamStatusFactory;

    @Inject
    private JsonEnvelopeJdbcRepository jsonEnvelopeJdbcRepository;

    @Inject
    private ProgressLogger progressLogger;

    @TransactionAttribute(NOT_SUPPORTED)
    public UUID dispatch(final UUID streamId) {

        progressLogger.logStart(streamId);

        replayAllEventsOf(streamId);

        insertStreamStatus(streamStatusFactory.create(
                jsonEnvelopeJdbcRepository.getLatestEvent(streamId),
                streamId));

        progressLogger.logCompletion(streamId);

        return streamId;
    }

    private void replayAllEventsOf(final UUID streamId) {
        final long lastPosition = jsonEnvelopeJdbcRepository.getCurrentVersion(streamId);

        for (long position = FIRST_POSITION; position <= lastPosition; position = position + PAGE_SIZE) {
            replayBatchOfEvents(streamId, position);
        }
    }


    @TransactionAttribute(REQUIRED)
    private void replayBatchOfEvents(final UUID streamId, final long position) {
        try (final Stream<JsonEnvelope> eventStream = jsonEnvelopeJdbcRepository.pageEventStream(streamId, position, PAGE_SIZE)) {
            eventStream.forEach(jsonEnvelope -> dispatchEnvelope(jsonEnvelope, streamId));
        }
    }

    private void dispatchEnvelope(final JsonEnvelope jsonEnvelope, final UUID streamId) {
        try {
            envelopeDispatcher.dispatch(jsonEnvelope);
            progressLogger.logSuccess(streamId, jsonEnvelope);
        } catch (final MissingHandlerException ex) {
            progressLogger.logFailure(streamId, jsonEnvelope);
        }
    }

    @TransactionAttribute(REQUIRED)
    private void insertStreamStatus(final StreamStatus streamStatus) {
        streamStatusRepository.insert(streamStatus);
    }
}
