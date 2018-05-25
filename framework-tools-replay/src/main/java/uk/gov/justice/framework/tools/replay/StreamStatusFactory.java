package uk.gov.justice.framework.tools.replay;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

public class StreamStatusFactory {

    public StreamStatus create(final JsonEnvelope jsonEnvelope, final UUID streamId) {
        final Long version = getVersionFrom(jsonEnvelope);
        final String source = getSourceFrom(jsonEnvelope);

        return new StreamStatus(streamId, version, source);
    }

    private Long getVersionFrom(final JsonEnvelope envelope) {
        return envelope
                .metadata()
                .version()
                .orElseThrow(() -> new IllegalArgumentException(format("Version not found in the envelope: %s", envelope.toString())));
    }

    private String getSourceFrom(final JsonEnvelope envelope) {
        return substringBefore(envelope.metadata().name(), ".");
    }
}
