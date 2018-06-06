package uk.gov.justice.framework.tools.replay;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;


@ApplicationScoped
public class TransactionalEnvelopeDispatcher {

    @Inject
    DispatcherCache dispatcherCache;

    private Dispatcher dispatcher;

    void register(@Observes final ServiceComponentFoundEvent event) {
        if (null == dispatcher) {
            dispatcher = dispatcherCache.dispatcherFor(event.getComponentName(), LOCAL);
        }
    }

    @Transactional(REQUIRES_NEW)
    public void dispatch(JsonEnvelope envelope) {
        dispatcher.dispatch(envelope);
    }
}
