package uk.gov.justice.replay.event.replayer;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;


@RunWith(MockitoJUnitRunner.class)
public class EventDispatcherTest {

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private Dispatcher dispatcher;

    @InjectMocks
    private EventDispatcher eventDispatcher;


    @Test
    public void shouldFindTheCorrectDispatcherInTheCacheAndDispatch() throws Exception {

        final JsonEnvelope eventEnvelope = mock(JsonEnvelope.class);

        when(dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL)).thenReturn(dispatcher);

        eventDispatcher.dispatchEvent(eventEnvelope);

        verify(dispatcher).dispatch(eventEnvelope);
    }
}
