package uk.gov.justice.framework.tools.replay;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncStreamDispatcherTest {

    @Mock
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    @Mock
    private StreamEnvelopeProvider streamEnvelopeProvider;

    @Mock
    private StreamStatusFactory streamStatusFactory;

    @Mock
    private ProgressLogger progressLogger;

    @InjectMocks
    private AsyncStreamDispatcher asyncStreamDispatcher;

    @Test
    public void shouldGetTheEventsOfAStreamDispatchThemAllThenUpdateTheStreamStatus() throws Exception {
        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);
        final List<JsonEnvelope> envelopes = asList(jsonEnvelope_1, jsonEnvelope_2, jsonEnvelope_3);

        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(streamEnvelopeProvider.getStreamAsList(streamId)).thenReturn(envelopes);
        when(streamStatusFactory.create(envelopes, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_1);
        inOrder.verify(progressLogger).logSuccess(streamId, 0);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_2);
        inOrder.verify(progressLogger).logSuccess(streamId, 1);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_3);
        inOrder.verify(progressLogger).logSuccess(streamId, 2);
        inOrder.verify(streamStatusRepository).insert(streamStatus);
        inOrder.verify(progressLogger).logCompletion(streamId);
    }

    @Test
    public void shouldLogFailureIfNoHandlerFoundForDispatch() throws Exception {

        final MissingHandlerException missingHandlerException = new MissingHandlerException("Ooops");

        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final List<JsonEnvelope> envelopes = singletonList(jsonEnvelope);

        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(streamEnvelopeProvider.getStreamAsList(streamId)).thenReturn(envelopes);
        doThrow(missingHandlerException).when(envelopeDispatcher).dispatch(jsonEnvelope);
        when(streamStatusFactory.create(envelopes, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        inOrder.verify(progressLogger).logFailure(streamId, jsonEnvelope);
        inOrder.verify(progressLogger).logCompletion(streamId);
    }
}
