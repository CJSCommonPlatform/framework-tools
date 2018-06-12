package uk.gov.justice.framework.tools.replay;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ProgressLogger {

    @Inject
    private ProgressChecker progressChecker;

    @Inject
    private JsonEnvelopeUtil jsonEnvelopeUtil;

    @Inject
    private Logger logger;

    private int sucessCount = 0;

    public void logStart(final UUID streamId) {
        logger.info("Starting processing of stream: {}", streamId);
    }

    public void logDispatch() {
        logger.info("Dispatching event");
    }

    public void logSuccess(final UUID streamId, final JsonEnvelope jsonEnvelope) {

        sucessCount++;

        if (progressChecker.shouldLogProgress(jsonEnvelopeUtil.versionOf(jsonEnvelope))) {
            logger.info("Processed {} element(s) of stream: {}", sucessCount, streamId);
        }
    }

    public void logFailure(final UUID streamId, final JsonEnvelope jsonEnvelope) {
        final Metadata metadata = jsonEnvelope.metadata();
        logger.warn("Missing handler for stream Id: {}, event name: {}, version: {}",
                streamId,
                metadata.name(),
                metadata.version().map(theVersion -> "" + theVersion).orElse("Not set")
        );
    }

    public void logCompletion(final UUID streamId) {
        logger.info("Finished processing of stream: {}. Processed {} elements", streamId, sucessCount);
    }
}
