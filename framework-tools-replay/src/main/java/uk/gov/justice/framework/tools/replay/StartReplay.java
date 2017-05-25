package uk.gov.justice.framework.tools.replay;

import static java.lang.System.currentTimeMillis;

import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class StartReplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartReplay.class);

    @Inject
    private JdbcEventRepository jdbcEventRepository;

    @Inject
    private AsyncStreamDispatcher asyncStreamDispatcher;

    boolean started = false;

    private final Deque<Future<Void>> dispatchResults = new LinkedList<>();


    @PostConstruct
    void go() throws InterruptedException {
        final long startTimeMillis = currentTimeMillis();
        LOGGER.info("-------------- Replay Event Streams -------------!");

        try (final Stream<Stream<JsonEnvelope>> streamOfEventStreams = jdbcEventRepository.getStreamOfAllEventStreams()) {
            streamOfEventStreams.forEach(s -> dispatchResults.add(asyncStreamDispatcher.dispatch(s)));
            started = true;

            while (!finished()) {
                Thread.sleep(3000);
            }
        }

        LOGGER.info("-------------- Replay of Event Streams Completed in {} milliseconds--------------", currentTimeMillis() - startTimeMillis);
    }


    boolean finished() {
        dispatchResults.removeIf(Future::isDone);
        return started && dispatchResults.stream().allMatch(Future::isDone);
    }
}