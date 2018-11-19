package uk.gov.justice.framework.tools.replay;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public class StreamSubscriptionFactory {

    public Subscription create(final JsonEnvelope jsonEnvelope, final UUID streamId) {
        final Long position = getPositionFrom(jsonEnvelope);
        final String source = getSourceFrom(jsonEnvelope);

        return new Subscription(streamId, position, source);
    }

    private Long getPositionFrom(final JsonEnvelope envelope) {
        return envelope
                .metadata()
                .version()
                .orElseThrow(() -> new IllegalArgumentException(format("Version not found in the envelope: %s", envelope.toString())));
    }

    private String getSourceFrom(final JsonEnvelope envelope) {
        return substringBefore(envelope.metadata().name(), ".");
    }
}
