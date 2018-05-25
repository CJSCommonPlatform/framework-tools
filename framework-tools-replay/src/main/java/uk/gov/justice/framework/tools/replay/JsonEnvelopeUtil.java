package uk.gov.justice.framework.tools.replay;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class JsonEnvelopeUtil {

    public Long versionOf(final JsonEnvelope jsonEnvelope) {
        return jsonEnvelope
                .metadata()
                .version()
                .orElse(-1L);
    }
}
