package uk.gov.justice.framework.tools.listener;

import uk.gov.justice.framework.tools.entity.TestEvent;
import uk.gov.justice.framework.tools.repository.TestViewstoreRepository;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(value = Component.EVENT_LISTENER)
public class FrameworkToolsTestListener {

    @Inject
    private TestViewstoreRepository testViewstoreRepository;

    @Handles("framework.example-test")
    public void handle(final JsonEnvelope envelope) {
        testViewstoreRepository.save(fromJsonEnvelope(envelope));
    }

    private TestEvent fromJsonEnvelope(JsonEnvelope envelope) {

        return new TestEvent(
                        envelope.metadata().id(),
                envelope.metadata().version().orElse(null).intValue(),
                        envelope.payloadAsJsonObject().toString());
    }
}
