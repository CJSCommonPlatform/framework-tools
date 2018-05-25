package uk.gov.justice.framework.tools.listener;

import uk.gov.justice.framework.tools.entity.TestEvent;
import uk.gov.justice.framework.tools.repository.TestViewstoreRepository;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(value = Component.EVENT_LISTENER)
public class FrameworkToolsTestListener {

    @Inject
    private Logger logger;

    @Inject
    private TestViewstoreRepository testViewstoreRepository;

    @Handles("framework.example-test")
    public void handle(final JsonEnvelope envelope) {

        logger.error("Saving envelope...");
        testViewstoreRepository.save(fromJsonEnvelope(envelope));
        logger.error("Envelope saved");
    }

    private TestEvent fromJsonEnvelope(JsonEnvelope envelope) {

        return new TestEvent(
                        envelope.metadata().id(),
                envelope.metadata().version().orElse(0L).intValue(),
                        envelope.payloadAsJsonObject().toString());
    }
}
