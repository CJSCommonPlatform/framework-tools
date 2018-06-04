package uk.gov.justice.framework.tools.replay.events;

import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventBuilder {

    public <T> Event eventFrom(final String eventName, final User user, final UUID eventStreamId, final long sequenceId) {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(ZonedDateTime.now())
                        .withVersion(sequenceId)
                        .withStreamId(eventStreamId))
                .withPayloadOf(user.getUserId(), "userId")
                .withPayloadOf(user.getFirstName(), "firstName")
                .withPayloadOf(user.getLastName(), "lastName")
                .build();

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID id = metadata.id();

        final UUID streamId = metadata.streamId().orElse(null);
        final String name = metadata.name();
        final String payload = jsonEnvelope.payloadAsJsonObject().toString();
        final ZonedDateTime createdAt = metadata.createdAt().orElse(null);

        return new Event(id, streamId, sequenceId, name, metadata.asJsonObject().toString(), payload, createdAt);
    }
}
