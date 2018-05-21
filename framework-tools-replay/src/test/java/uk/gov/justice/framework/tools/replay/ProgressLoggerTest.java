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

        when(progressChecker.shouldLogProgress(0)).thenReturn(true);
        when(progressChecker.shouldLogProgress(10)).thenReturn(true);

        for(int i = 0; i < 11; i++) {
            progressLogger.logSuccess(streamId, i);
        }

        verify(logger).info("Processed {} element(s) of stream: {}", 1, streamId);
        verify(logger).info("Processed {} element(s) of stream: {}", 11, streamId);
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

        final UUID streamId = UUID.randomUUID();

        for(int i = 0; i < sucessCount; i++) {
            progressLogger.logSuccess(streamId, i);
        }

        progressLogger.logCompletion(streamId);

        verify(logger).info("Finished processing of stream: {}. Processed {} elements", streamId, sucessCount);
    }
}
