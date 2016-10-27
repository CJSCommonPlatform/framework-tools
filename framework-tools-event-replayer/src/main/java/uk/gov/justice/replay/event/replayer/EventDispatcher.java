package uk.gov.justice.replay.event.replayer;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventDispatcher {

    @Inject
    DispatcherCache dispatcherCache;

    public void dispatchEvent(final JsonEnvelope eventEnvelope) {

        final Dispatcher dispatcher = dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL);
        dispatcher.dispatch(eventEnvelope);
    }
}
