package uk.gov.justice.framework.tools.replay;

import static java.lang.String.format;
import static org.wildfly.swarm.bootstrap.Main.MAIN_PROCESS_FILE;

import org.slf4j.Logger;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.inject.Inject;

import java.io.File;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

@Singleton
@Startup
public class StartReplay implements ManagedTaskListener {

    private static final String NO_PROCESS_FILE_WARNING = "!!!!! No Swarm Process File specific, application will not auto-shutdown on completion. Please use option '-Dorg.wildfly.swarm.mainProcessFile=/pathTo/aFile' to specify location of process file with read/write permissions !!!!!";

    @Inject
    private Logger logger;

    @Resource
    private ManagedExecutorService executorService;

    @Inject
    private JdbcEventRepository jdbcEventRepository;
    @Inject
    private AsyncStreamDispatcher asyncStreamDispatcher;
    private Deque<Future<UUID>> outstandingTasks = new LinkedBlockingDeque<>();

    private boolean allTasksCreated = false;

    @PostConstruct
    void go() {
        logger.info("-------------- Invoke Event Streams Replay-------------!");

        checkForMainProcessFile();

        jdbcEventRepository.getStreamOfAllEventStreams()
                .forEach(eventStream -> {
                    StreamDispatchTask dispatchTask = new StreamDispatchTask(eventStream, asyncStreamDispatcher, this);
                    outstandingTasks.add(executorService.submit(dispatchTask));
                });
        allTasksCreated = true;
        if (outstandingTasks.isEmpty()) shutdown();
        logger.info("-------------- Invocation of Event Streams Replay Completed --------------");
    }

    @Override
    public void taskAborted(final Future<?> dispatchTaskFuture, final ManagedExecutorService managedExecutorService, final Object dispatchTask, final Throwable throwable) {
        logger.debug("Dispatch task aborted");
        boolean dispatchComplete = removeOutstandingTask(dispatchTaskFuture) && allTasksCreated;
        if (dispatchComplete) shutdown();
    }

    @Override
    public void taskDone(final Future<?> dispatchTaskFuture, final ManagedExecutorService managedExecutorService, final Object dispatchTask, final Throwable throwable) {
        logger.debug("Dispatch task done");
        boolean dispatchComplete = removeOutstandingTask(dispatchTaskFuture) && allTasksCreated;
        if (dispatchComplete) shutdown();
    }

    @Override
    public void taskStarting(final Future<?> dispatchTaskFuture, final ManagedExecutorService managedExecutorService, final Object dispatchTask) {
        logger.debug("Starting Dispatch task");
    }

    @Override
    public void taskSubmitted(final Future<?> dispatchTaskFuture, final ManagedExecutorService managedExecutorService, final Object dispatchTask) {
        logger.debug("Submitted Dispatch task");
    }

    private boolean removeOutstandingTask(final Future<?> dispatchTaskFuture) {
        outstandingTasks.remove(dispatchTaskFuture);
        return outstandingTasks.isEmpty();
    }

    private void shutdown() {
        logger.info("========== ALL TASKS HAVE BEEN DISPATCHED -- ATTEMPTING SHUTDOWN =================");
        final String processFile = System.getProperty(MAIN_PROCESS_FILE);
        if (processFile != null) {
            final File uuidFile = new File(processFile);
            if (uuidFile.exists()) {
                uuidFile.delete();
            } else {
                logger.warn(format("Failed to delete process file '%s', file does not exist", processFile));
            }
        }

        logger.info("========== ALL TASKS HAVE BEEN DISPATCHED -- SHUTDOWN =================");
    }

    private void checkForMainProcessFile() {
        if (System.getProperty(MAIN_PROCESS_FILE) == null) {
            logger.warn(NO_PROCESS_FILE_WARNING);
        }
    }
}