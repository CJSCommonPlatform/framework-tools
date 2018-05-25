package uk.gov.justice.framework.tools.replay;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;


@ApplicationScoped
public class TransactionalEnvelopeDispatcher {
    @Inject
    DispatcherCache dispatcherCache;

    private Dispatcher dispatcher;

    @PostConstruct
    public void init() {
        dispatcher = dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL);
    }

    @Transactional(REQUIRES_NEW)
    public void dispatch(JsonEnvelope envelope) {
        dispatcher.dispatch(envelope);
    }
}
