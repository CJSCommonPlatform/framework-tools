package uk.gov.justice.replay.event.replayer;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;


@RunWith(MockitoJUnitRunner.class)
public class EventReplayerTest {

    @Mock
    private EventDispatcher eventDispatcher;

    @InjectMocks
    private EventReplayer eventReplayer;

    @Test
    public void shouldCallTheEventHandlerForEachEventEnvelope() throws Exception {

        final JsonEnvelope eventEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope eventEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope eventEnvelope_3 = mock(JsonEnvelope.class);

        final Stream<JsonEnvelope> eventStream = Stream.of(eventEnvelope_1, eventEnvelope_2, eventEnvelope_3);

        eventReplayer.replay(eventStream);

        verify(eventDispatcher).dispatchEvent(eventEnvelope_1);
        verify(eventDispatcher).dispatchEvent(eventEnvelope_2);
        verify(eventDispatcher).dispatchEvent(eventEnvelope_3);
    }
}
