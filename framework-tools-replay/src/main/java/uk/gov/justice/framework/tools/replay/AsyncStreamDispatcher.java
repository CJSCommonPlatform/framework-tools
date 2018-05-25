package uk.gov.justice.framework.tools.replay;

import static java.util.stream.IntStream.range;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

@Stateless
public class AsyncStreamDispatcher {

    @Inject
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Inject
    private StreamEnvelopeProvider streamEnvelopeProvider;

    @Inject
    private StreamStatusFactory streamStatusFactory;

    @Inject
    private ProgressLogger progressLogger;

    @TransactionAttribute(NOT_SUPPORTED)
    public UUID dispatch(final UUID streamId) {

        progressLogger.logStart(streamId);

        final List<JsonEnvelope> envelopes = streamEnvelopeProvider.getStreamAsList(streamId);

        range(0, envelopes.size())
                .forEach(index -> dispatchEnvelope(
                        envelopes.get(index),
                        streamId,
                        index));

        insertStreamStatus(streamStatusFactory.create(envelopes, streamId));

        progressLogger.logCompletion(streamId);

        return streamId;
    }

    @TransactionAttribute(REQUIRED)
    private void dispatchEnvelope(final JsonEnvelope jsonEnvelope, final UUID streamId, final int index) {
        try {
            envelopeDispatcher.dispatch(jsonEnvelope);
            progressLogger.logSuccess(streamId, index);
        } catch (final MissingHandlerException ex) {
            progressLogger.logFailure(streamId, jsonEnvelope);
        }
    }

    @TransactionAttribute(REQUIRED)
    private void insertStreamStatus(final StreamStatus streamStatus) {
        streamStatusRepository.insert(streamStatus);
    }
}
