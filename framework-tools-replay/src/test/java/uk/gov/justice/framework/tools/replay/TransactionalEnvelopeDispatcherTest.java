package uk.gov.justice.framework.tools.replay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import static org.mockito.Mockito.*;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;


@RunWith(MockitoJUnitRunner.class)
public class TransactionalEnvelopeDispatcherTest {

    @Mock
    private DispatcherCache dispatcherCache;

    @InjectMocks
    private TransactionalEnvelopeDispatcher transactionalEnvelopeDispatcher;

    @Test
    public void shouldDispatchEnvelope() {
        Dispatcher dispatcher = mock(Dispatcher.class);

        when(dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL)).thenReturn(dispatcher);
        transactionalEnvelopeDispatcher.init();

        final JsonEnvelope envelope = envelope().build();
        transactionalEnvelopeDispatcher.dispatch(envelope);
        verify(dispatcher).dispatch(envelope);

    }
}