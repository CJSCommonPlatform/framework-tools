package uk.gov.justice.framework.tools.replay;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TransactionalEnvelopeDispatcherTest {

    @Mock
    private DispatcherCache dispatcherCache;

    @InjectMocks
    private TransactionalEnvelopeDispatcher transactionalEnvelopeDispatcher;

    @Test
    public void shouldDispatchEnvelope() {
        final Dispatcher dispatcher = mock(Dispatcher.class);
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final ServiceComponentFoundEvent serviceComponentFoundEventClass = mock(ServiceComponentFoundEvent.class);

        when(serviceComponentFoundEventClass.getComponentName()).thenReturn(EVENT_LISTENER);
        when(dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL)).thenReturn(dispatcher);

        transactionalEnvelopeDispatcher.register(serviceComponentFoundEventClass);

        transactionalEnvelopeDispatcher.dispatch(envelope);

        verify(dispatcher).dispatch(envelope);
    }
}
