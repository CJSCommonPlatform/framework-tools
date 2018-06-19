package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.rangeClosed;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

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

    @Mock
    private LoggingMdc loggingMdc;

    @Mock
    private Logger logger;

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

        when(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId)).thenReturn(3L);
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1L, PAGE_SIZE)).thenReturn(envelopeStream);
        when(jsonEnvelopeJdbcRepository.getLatestEvent(streamId)).thenReturn(jsonEnvelope_3);
        when(streamStatusFactory.create(jsonEnvelope_3, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(loggingMdc, progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(loggingMdc).put("streamId", "streamId: " + streamId);
        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(loggingMdc).put("eventData", "event: " + jsonEnvelope_1.toString());
        inOrder.verify(progressLogger).logDispatch();
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_1);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_1);
        inOrder.verify(loggingMdc).remove("eventData");

        inOrder.verify(loggingMdc).put("eventData", "event: " + jsonEnvelope_2.toString());
        inOrder.verify(progressLogger).logDispatch();
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_2);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_2);
        inOrder.verify(loggingMdc).remove("eventData");

        inOrder.verify(loggingMdc).put("eventData", "event: " + jsonEnvelope_3.toString());
        inOrder.verify(progressLogger).logDispatch();
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope_3);
        inOrder.verify(progressLogger).logSuccess(streamId, jsonEnvelope_3);
        inOrder.verify(loggingMdc).remove("eventData");

        inOrder.verify(streamStatusRepository).insert(streamStatus);
        inOrder.verify(progressLogger).logCompletion(streamId);
        inOrder.verify(loggingMdc).clear();
    }

    @Test
    public void shouldDispatchOneFullPageOfEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<JsonEnvelope> pageOfEvents_1 = pageOfJsonEnvelopes(PAGE_SIZE);

        final JsonEnvelope lastEnvelope = pageOfEvents_1.get(PAGE_SIZE - 1);
        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId)).thenReturn(1000L);
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1L, PAGE_SIZE)).thenReturn(pageOfEvents_1.stream());
        when(jsonEnvelopeJdbcRepository.getLatestEvent(streamId)).thenReturn(lastEnvelope);
        when(streamStatusFactory.create(lastEnvelope, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(envelopeDispatcher, streamStatusRepository);

        for (JsonEnvelope jsonEnvelope : pageOfEvents_1) {
            inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        }

        inOrder.verify(streamStatusRepository).insert(streamStatus);
    }

    @Test
    public void shouldDispatchTwoFullPagesOfEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<JsonEnvelope> pageOfEvents_1 = pageOfJsonEnvelopes(PAGE_SIZE);
        final List<JsonEnvelope> pageOfEvents_2 = pageOfJsonEnvelopes(PAGE_SIZE);

        final JsonEnvelope lastEnvelope = pageOfEvents_2.get(PAGE_SIZE - 1);
        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId)).thenReturn(2000L);
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1L, PAGE_SIZE)).thenReturn(pageOfEvents_1.stream());
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1001L, PAGE_SIZE)).thenReturn(pageOfEvents_2.stream());
        when(jsonEnvelopeJdbcRepository.getLatestEvent(streamId)).thenReturn(lastEnvelope);
        when(streamStatusFactory.create(lastEnvelope, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(envelopeDispatcher, streamStatusRepository);

        for (JsonEnvelope jsonEnvelope : pageOfEvents_1) {
            inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        }

        for (JsonEnvelope jsonEnvelope : pageOfEvents_2) {
            inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        }

        inOrder.verify(streamStatusRepository).insert(streamStatus);
    }

    @Test
    public void shouldDispatchOneFullPageAndSecondPageWithSingleEvent() throws Exception {
        final UUID streamId = randomUUID();

        final List<JsonEnvelope> pageOfEvents_1 = pageOfJsonEnvelopes(PAGE_SIZE);
        final List<JsonEnvelope> pageOfEvents_2 = pageOfJsonEnvelopes(1);

        final JsonEnvelope lastEnvelope = pageOfEvents_2.get(0);
        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId)).thenReturn(1001L);
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1L, PAGE_SIZE)).thenReturn(pageOfEvents_1.stream());
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1001L, PAGE_SIZE)).thenReturn(pageOfEvents_2.stream());
        when(jsonEnvelopeJdbcRepository.getLatestEvent(streamId)).thenReturn(lastEnvelope);
        when(streamStatusFactory.create(lastEnvelope, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(envelopeDispatcher, streamStatusRepository);

        for (JsonEnvelope jsonEnvelope : pageOfEvents_1) {
            inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        }

        for (JsonEnvelope jsonEnvelope : pageOfEvents_2) {
            inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        }

        inOrder.verify(streamStatusRepository).insert(streamStatus);
    }

    @Test
    public void shouldLogFailureIfNoHandlerFoundForDispatch() throws Exception {

        final MissingHandlerException missingHandlerException = new MissingHandlerException("Ooops");

        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Stream<JsonEnvelope> envelopeStream = Stream.of(jsonEnvelope);

        final StreamStatus streamStatus = mock(StreamStatus.class);

        when(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId)).thenReturn(1L);
        when(jsonEnvelopeJdbcRepository.pageEventStream(streamId, 1L, PAGE_SIZE)).thenReturn(envelopeStream);
        doThrow(missingHandlerException).when(envelopeDispatcher).dispatch(jsonEnvelope);
        when(streamStatusFactory.create(jsonEnvelope, streamId)).thenReturn(streamStatus);

        assertThat(asyncStreamDispatcher.dispatch(streamId), is(streamId));

        final InOrder inOrder = inOrder(progressLogger, envelopeDispatcher, streamStatusRepository, progressLogger);

        inOrder.verify(progressLogger).logStart(streamId);
        inOrder.verify(envelopeDispatcher).dispatch(jsonEnvelope);
        inOrder.verify(progressLogger).logFailure(streamId, jsonEnvelope);
        inOrder.verify(progressLogger).logCompletion(streamId);
    }

    private List<JsonEnvelope> pageOfJsonEnvelopes(final int numberOfEvents) {
        final List<JsonEnvelope> pageOfEvents = new ArrayList<>();

        rangeClosed(1, numberOfEvents).forEach(value -> {
            pageOfEvents.add(mock(JsonEnvelope.class));
        });

        return pageOfEvents;
    }
}
