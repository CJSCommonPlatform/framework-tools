package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wildfly.swarm.bootstrap.Main.MAIN_PROCESS_FILE;

import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class StartReplayTest {

    @Mock
    private JdbcEventRepository jdbcEventRepository;

    @Mock
    private ManagedExecutorService executorService;

    @Mock
    private StreamDispatchTask dispatchTask;

    @Mock
    private Throwable throwable;

    @Mock
    private Future<UUID> dispatchTaskFuture;

    @Mock
    private Deque<UUID> outstandingTasks;

    @Mock
    private Logger logger;

    @InjectMocks
    private StartReplay startReplay;

    @Test
    public void shouldDispatchStreams() throws IOException {
        final Stream<UUID> activeStreamIds = Stream.of(randomUUID(), randomUUID());
        createMainProcessFile();

        when(jdbcEventRepository.getAllActiveStreamIds()).thenReturn(activeStreamIds);
        when(executorService.submit(any(StreamDispatchTask.class))).thenReturn(dispatchTaskFuture);
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.go();

        verify(executorService, times(2)).submit(any(StreamDispatchTask.class));
        verify(logger).info("-------------- Invoke Event Streams Replay-------------!");
    }

    @Test
    public void shouldLogWarningIfFailedToDeleteProcessFileDispatchStreams() throws IOException {
        final Stream<UUID> activeStreamIds = Stream.of(randomUUID(), randomUUID());

        System.setProperty(MAIN_PROCESS_FILE, Paths.get("src/test/processFile").toString());

        when(jdbcEventRepository.getAllActiveStreamIds()).thenReturn(activeStreamIds);
        when(executorService.submit(any(StreamDispatchTask.class))).thenReturn(dispatchTaskFuture);
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.go();

        verify(executorService, times(2)).submit(any(StreamDispatchTask.class));
        verify(logger).warn("Failed to delete process file '{0}', file does not exist", "src/test/processFile");
    }

    @Test
    public void shouldDispatchStreamsAndShutdownByForce() {
        final Stream<UUID> activeStreamIds = Stream.of(randomUUID(), randomUUID());

        when(jdbcEventRepository.getAllActiveStreamIds()).thenReturn(activeStreamIds);
        when(executorService.submit(any(StreamDispatchTask.class))).thenReturn(dispatchTaskFuture);
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.go();

        verify(executorService, times(2)).submit(any(StreamDispatchTask.class));
        verify(logger).info("-------------- Invoke Event Streams Replay-------------!");
    }

    @Test
    public void shouldRemoveAbortedTask() {
        startReplay.taskAborted(dispatchTaskFuture, executorService, dispatchTask, throwable);

        verify(outstandingTasks, times(1)).remove(eq(dispatchTaskFuture));
        verify(outstandingTasks, times(1)).isEmpty();
    }

    @Test
    public void shouldRemoveCompletedTask() {
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.taskDone(dispatchTaskFuture, executorService, dispatchTask, throwable);

        verify(outstandingTasks, times(1)).remove(eq(dispatchTaskFuture));
        verify(outstandingTasks, times(1)).isEmpty();
    }

    @Test
    public void shouldLogTaskStarting() {
        startReplay.taskStarting(dispatchTaskFuture, executorService, dispatchTask);

        verify(logger).debug(eq("Starting Dispatch task"));
    }

    @Test
    public void shouldLogTaskSubmitted() {
        startReplay.taskSubmitted(dispatchTaskFuture, executorService, dispatchTask);

        verify(logger).debug(eq("Submitted Dispatch task"));
    }

    private void createMainProcessFile() throws IOException {
        final Path file = Files.createFile(Paths.get("src/test/processFile"));
        System.setProperty(MAIN_PROCESS_FILE, file.toString());
    }
}
