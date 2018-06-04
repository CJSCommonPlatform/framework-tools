package uk.gov.justice.framework.tools.replay;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class JsonEnvelopeJdbcRepository {

    @Inject
    private EventSource eventSource;

    public Stream<JsonEnvelope> pageEventStream(final UUID streamId, final long position, final long pageSize) {
        return eventSource
                .getStreamById(streamId)
                .readFrom(position)
                .limit(pageSize);
    }

    public JsonEnvelope getLatestEvent(final UUID streamId) {

        final EventStream eventStream = eventSource.getStreamById(streamId);
        final long currentVersion = eventStream.getCurrentVersion();

        try(final Stream<JsonEnvelope> jsonEnvelopeStream = eventStream.readFrom(currentVersion)) {
            return jsonEnvelopeStream
                    .findFirst()
                    .orElseThrow(() -> new MissingEventStreamHeadException(format("Unable to retrieve head Event from stream with id '%s'", streamId)));
        }
    }

    public long getCurrentVersion(final UUID streamId) {
        return eventSource
                .getStreamById(streamId)
                .getCurrentVersion();
    }
}
