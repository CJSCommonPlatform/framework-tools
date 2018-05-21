package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AsyncStreamDispatcherTest {

    @Mock
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    @InjectMocks
    private AsyncStreamDispatcher asyncStreamDispatcher;

    @Test
    public void shouldDispatchEnvelopes() {

        final UUID streamId = randomUUID();
        final JsonEnvelope envelope1 = envelope().with(
                metadataWithRandomUUID("source.event-occurred")
                        .withStreamId(streamId)
                        .withVersion(1L))
                .build();
        final JsonEnvelope envelope2 = envelope().with(
                metadataWithRandomUUID("source.another-event-occurred")
                        .withStreamId(streamId)
                        .withVersion(2L))
                .build();

        doNothing().when(envelopeDispatcher).dispatch(envelope1);
        doNothing().when(envelopeDispatcher).dispatch(envelope2);

//        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2));

        final ArgumentCaptor<JsonEnvelope> dispatchCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(envelopeDispatcher, times(2)).dispatch(dispatchCaptor.capture());
        final List<JsonEnvelope> dispatchedEnvelopes = dispatchCaptor.getAllValues();

        assertThat(dispatchedEnvelopes, contains(envelope1, envelope2));
    }

    @Test
    public void shouldUpdateStreamBufferStatus() {

        final UUID streamId = randomUUID();
        final JsonEnvelope envelope1 = JsonEnvelopeBuilder.envelope().with(
                metadataWithRandomUUID("source.event-occurred")
                        .withStreamId(streamId)
                        .withVersion(4L))
                .build();
        final JsonEnvelope envelope2 = JsonEnvelopeBuilder.envelope().with(
                metadataWithRandomUUID("source.another-event-occurred")
                        .withStreamId(streamId)
                        .withVersion(5L))
                .build();

//        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2));

        verify(streamStatusRepository).insert(new StreamStatus(streamId, 5L, "source"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoStreamIdInTheEnvelope() {

        final Stream<JsonEnvelope> stream = Stream.of(
                envelope()
                        .with(metadataWithRandomUUID("dummyName")
                                .withVersion(1L))
                        .build());

//        asyncStreamDispatcher.dispatch(stream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoVersionInTheEnvelope() {

        final Stream<JsonEnvelope> stream = Stream.of(
                envelope()
                        .with(metadataWithDefaults()
                                .withStreamId(randomUUID()))
                        .build());

//        asyncStreamDispatcher.dispatch(stream);
    }

    @Test
    public void shouldProcessStreamWhenThereIsNoHandlerDefined() {

        final UUID streamId = randomUUID();

        doThrow(new MissingHandlerException("Handler for event-without-handler not found"))
                .when(envelopeDispatcher).dispatch(
                argThat(jsonEnvelope()
                        .withMetadataOf(metadata().withName("source.event-without-handler"))));

        final JsonEnvelope envelope1 = envelope()
                .with(metadataWithRandomUUID("source.event-with-handler")
                        .withStreamId(streamId)
                        .withVersion(1L))
                .build();
        final JsonEnvelope envelope2 = envelope()
                .with(metadataWithRandomUUID("source.event-without-handler")
                        .withStreamId(streamId)
                        .withVersion(2L)).build();
        final JsonEnvelope envelope3 = envelope()
                .with(metadataWithRandomUUID("source.event-with-handler")
                        .withStreamId(streamId)
                        .withVersion(3L))
                .build();

//        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2, envelope3));

        final ArgumentCaptor<JsonEnvelope> dispatchCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(envelopeDispatcher, times(3)).dispatch(dispatchCaptor.capture());
        verify(streamStatusRepository).insert(new StreamStatus(streamId, 3L, "source"));
    }
}
