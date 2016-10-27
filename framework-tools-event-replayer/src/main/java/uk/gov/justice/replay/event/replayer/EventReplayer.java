package uk.gov.justice.replay.event.replayer;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventReplayer {

    @Inject
    EventDispatcher eventDispatcher;

    public void replay(final Stream<JsonEnvelope> eventStream) {
        eventStream.forEach(eventDispatcher::dispatchEvent);
    }
}
