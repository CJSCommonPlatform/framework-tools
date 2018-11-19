package uk.gov.justice.framework.tools.replay;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class StreamEnvelopeProvider {

    @Inject
    private EventRepository eventRepository;

    public List<JsonEnvelope> getStreamAsList(final UUID streamId) {
        try (final Stream<JsonEnvelope> stream = eventRepository.getEventsByStreamId(streamId)) {
            return stream.collect(toList());
        }
    }
}
