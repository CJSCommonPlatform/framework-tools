package uk.gov.justice.framework.tools.listener;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.framework.tools.database.domain.User;
import uk.gov.justice.framework.tools.repository.TestViewstoreRepository;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

@ServiceComponent(value = EVENT_LISTENER)
public class FrameworkToolsTestListener {

    @Inject
    private TestViewstoreRepository testViewstoreRepository;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private Logger logger;

    @Handles("framework.update-user")
    public void handle(final JsonEnvelope envelope) {

        testViewstoreRepository.save(fromJsonEnvelope(envelope));
        logger.debug("Event saved");
    }

    private User fromJsonEnvelope(final JsonEnvelope envelope) {

        final String payload = envelope.payloadAsJsonObject().toString();

        try {
            return objectMapper.readValue(payload, User.class);
        } catch (final IOException e) {
            throw new RuntimeException(format("Failed to create User from json: '%s'", payload));
        }
    }
}
