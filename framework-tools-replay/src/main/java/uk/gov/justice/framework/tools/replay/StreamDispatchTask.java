package uk.gov.justice.framework.tools.replay;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StreamDispatchTask implements Callable<UUID>, ManagedTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDispatchTask.class);
    private final UUID streamId;
    private final AsyncStreamDispatcher dispatcher;
    private final ManagedTaskListener dispatchListener;

    public StreamDispatchTask(
            final UUID streamId,
            final AsyncStreamDispatcher dispatcher,
            final ManagedTaskListener dispatchListener) {
        this.streamId = streamId;
        this.dispatcher = dispatcher;
        this.dispatchListener = dispatchListener;
    }

    @Override
    public UUID call() {
        LOGGER.debug("---------- Dispatching stream -------------");

        return dispatcher.dispatch(streamId);
    }

    @Override
    public Map<String, String> getExecutionProperties() {
        return null;
    }

    @Override
    public ManagedTaskListener getManagedTaskListener() {
        return dispatchListener;
    }
}
