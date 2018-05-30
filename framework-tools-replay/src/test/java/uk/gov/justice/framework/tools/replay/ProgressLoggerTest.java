package uk.gov.justice.framework.tools.replay;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ProgressLoggerTest {

    @Mock
    private ProgressChecker progressChecker;

    @Mock
    private JsonEnvelopeUtil jsonEnvelopeUtil;

    @Mock
    private Logger logger;

    @InjectMocks
    private ProgressLogger progressLogger;

    @Test
    public void shouldLogTheStartOfTheProcess() throws Exception {

        final UUID streamId = randomUUID();

        progressLogger.logStart(streamId);

        verify(logger).info("Starting processing of stream: {}", streamId);
    }

    @Test
    public void shouldLogSuccessOnlyIfTheProgressCheckerAllowsIt() throws Exception {

        final UUID streamId = randomUUID();
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_4 = mock(JsonEnvelope.class);

        final Stream<JsonEnvelope> jsonEnvelopeStream = Stream.of(jsonEnvelope_1, jsonEnvelope_2, jsonEnvelope_3, jsonEnvelope_4);

        when(progressChecker.shouldLogProgress(1)).thenReturn(true);
        when(progressChecker.shouldLogProgress(4)).thenReturn(true);

        when(jsonEnvelopeUtil.versionOf(jsonEnvelope_1)).thenReturn(1L);
        when(jsonEnvelopeUtil.versionOf(jsonEnvelope_2)).thenReturn(2L);
        when(jsonEnvelopeUtil.versionOf(jsonEnvelope_3)).thenReturn(3L);
        when(jsonEnvelopeUtil.versionOf(jsonEnvelope_4)).thenReturn(4L);

        jsonEnvelopeStream.forEach(jsonEnvelope -> progressLogger.logSuccess(streamId, jsonEnvelope));

        verify(logger).info("Processed {} element(s) of stream: {}", 1, streamId);
        verify(logger).info("Processed {} element(s) of stream: {}", 4, streamId);
    }

    @Test
    public void shouldLogFailures() throws Exception {

        final UUID streamId = randomUUID();
        final String commandName = "example-command-api.notification-added";
        final long version = 1234L;

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        when(metadata.name()).thenReturn(commandName);
        when(metadata.version()).thenReturn(of(version));

        when(jsonEnvelope.metadata()).thenReturn(metadata);

        progressLogger.logFailure(streamId, jsonEnvelope);

        verify(logger).warn("Missing handler for stream Id: {}, event name: {}, version: {}",
                streamId,
                commandName,
                "1234"
        );
    }

    @Test
    public void shouldUseNotSetIfTheVersionDoesNotExistWhenLoggingFailures() throws Exception {

        final UUID streamId = randomUUID();
        final String commandName = "example-command-api.notification-added";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        when(metadata.name()).thenReturn(commandName);
        when(metadata.version()).thenReturn(empty());

        when(jsonEnvelope.metadata()).thenReturn(metadata);

        progressLogger.logFailure(streamId, jsonEnvelope);

        verify(logger).warn("Missing handler for stream Id: {}, event name: {}, version: {}",
                streamId,
                commandName,
                "Not set"
        );
    }

    @Test
    public void shouldLogCompletion() throws Exception {

        final int sucessCount = 10;
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        final UUID streamId = UUID.randomUUID();

        for (int i = 0; i < sucessCount; i++) {
            progressLogger.logSuccess(streamId, jsonEnvelope);
        }

        progressLogger.logCompletion(streamId);

        verify(logger).info("Finished processing of stream: {}. Processed {} elements", streamId, sucessCount);
    }
}
