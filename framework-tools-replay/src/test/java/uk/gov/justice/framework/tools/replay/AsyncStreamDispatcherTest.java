package uk.gov.justice.framework.tools.replay;

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

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncStreamDispatcherTest {

    private static final int PAGE_SIZE = 1000;

    @Mock
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    @Mock
    private StreamStatusFactory streamStatusFactory;

    @Mock
    private JsonEnvelopeJdbcRepository jsonEnvelopeJdbcRepository;

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

        final Stream<JsonEnvelope> envelopeStream = Stream.of(jsonEnvelope_1, jsonEnvelope_2, jsonEnvelope_3);
        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getLatestSequenceIdForStream(streamId)).thenReturn(3L);
        when(jsonEnvelopeJdbcRepository.forward(streamId, 1L, PAGE_SIZE)).thenReturn(envelopeStream);
        when(jsonEnvelopeJdbcRepository.head(streamId)).thenReturn(jsonEnvelope_3);
        when(streamStatusFactory.create(jsonEnvelope_3, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_1);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_1);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_2);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_2);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_3);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_3);
        inOrder.verify(streamStatusRepository).insert(streamStatus);
        inOrder.verify(progressLogger).logCompletion(streamId);
    }

    @Test
    public void shouldLogFailureIfNoHandlerFoundForDispatch() throws Exception {

        final MissingHandlerException missingHandlerException = new MissingHandlerException("Ooops");

        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Stream<JsonEnvelope> envelopeStream = Stream.of(jsonEnvelope);

        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getLatestSequenceIdForStream(streamId)).thenReturn(1L);
        when(jsonEnvelopeJdbcRepository.forward(streamId, 1L, PAGE_SIZE)).thenReturn(envelopeStream);
        doThrow(missingHandlerException).when(envelopeDispatcher).dispatch(jsonEnvelope);
        when(streamStatusFactory.create(jsonEnvelope, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        inOrder.verify(progressLogger).logFailure(streamId, jsonEnvelope);
        inOrder.verify(progressLogger).logCompletion(streamId);
    }
}
