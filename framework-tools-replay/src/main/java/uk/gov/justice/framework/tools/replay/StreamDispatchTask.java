package uk.gov.justice.framework.tools.replay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


public class StreamDispatchTask implements Callable<UUID>, ManagedTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDispatchTask.class);
    private final Stream<JsonEnvelope> stream;
    private final UUID streamId;
    private final AsyncStreamDispatcher dispatcher;
    private final ManagedTaskListener dispatchListener;

    public StreamDispatchTask(
            final UUID streamId,
            final Stream<JsonEnvelope> stream,
            final AsyncStreamDispatcher dispatcher,
            final ManagedTaskListener dispatchListener) {
        this.streamId = streamId;
        this.dispatcher = dispatcher;
        this.dispatchListener = dispatchListener;
        this.stream = stream;
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
