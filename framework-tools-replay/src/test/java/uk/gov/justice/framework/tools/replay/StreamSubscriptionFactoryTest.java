package uk.gov.justice.framework.tools.replay;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class StreamSubscriptionFactoryTest {

    @InjectMocks
    private StreamSubscriptionFactory streamSubscriptionFactory;

    @Test
    public void shouldCreateASubscriptionUsingTheValuesInTheFirstEnvelopeInTheList() throws Exception {

        final String commandName = "example-command-api.notification-added";
        final Optional<Long> position = of(29384L);
        final UUID streamId = randomUUID();
        final UUID envelopeId_1 = randomUUID();

        final JsonEnvelope jsonEnvelope_1 = anEnvelope(commandName, position, envelopeId_1, streamId);

        final Subscription subscription = streamSubscriptionFactory.create(jsonEnvelope_1, streamId);

        assertThat(subscription.getSource(), is("example-command-api"));
        assertThat(subscription.getStreamId(), is(streamId));
        assertThat(subscription.getPosition(), is(position.get()));
    }

    @Test
    public void shouldThrowAnIllegalArgumentExceptionIfTheEnvelopeDoesNotContainAVersion() throws Exception {

        final String commandName = "example-command-api.notification-added";
        final Optional<Long> position = empty();
        final UUID streamId = randomUUID();
        final UUID envelopeId = randomUUID();

        final JsonEnvelope jsonEnvelope = anEnvelope(commandName, position, envelopeId, streamId);

        try {
            streamSubscriptionFactory.create(jsonEnvelope, streamId);
            fail();
        } catch (final IllegalArgumentException expected) {
            final String message = "Version not found in the envelope: " +
                    "{\"id\":\"" + envelopeId + "\"," +
                    "\"name\":\"example-command-api.notification-added\"," +
                    "\"causation\":[]}";

            assertThat(expected.getMessage(), is(message));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private JsonEnvelope anEnvelope(final String commandName, final Optional<Long> position, final UUID envelopeId, final UUID streamId) {

        final MetadataBuilder metadataBuilder = metadataBuilder()
                .withId(envelopeId)
                .withName(commandName)
                .withStreamId(streamId);

        position.ifPresent(metadataBuilder::withVersion);

        return envelopeFrom(
                metadataBuilder,
                createObjectBuilder()
                        .add("exampleField", "example value"));
    }
}
