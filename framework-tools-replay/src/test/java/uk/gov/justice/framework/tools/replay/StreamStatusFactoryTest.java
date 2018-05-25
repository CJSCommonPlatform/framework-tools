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

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class StreamStatusFactoryTest {

    @InjectMocks
    private StreamStatusFactory streamStatusFactory;

    @Test
    public void shouldCreateAStreamStatusUsingTheValuesInTheFirstEnvelopeInTheList() throws Exception {

        final String commandName = "example-command-api.notification-added";
        final Optional<Long> version = of(29384L);
        final UUID streamId = randomUUID();
        final UUID envelopeId_1 = randomUUID();

        final JsonEnvelope jsonEnvelope_1 = anEnvelope(commandName, version, envelopeId_1, streamId);

        final StreamStatus streamStatus = streamStatusFactory.create(jsonEnvelope_1, streamId);

        assertThat(streamStatus.getSource(), is("example-command-api"));
        assertThat(streamStatus.getStreamId(), is(streamId));
        assertThat(streamStatus.getVersion(), is(version.get()));
    }

    @Test
    public void shouldThrowAnIllegalArgumentExceptionIfTheEnvelopeDoesNotContainAVersion() throws Exception {

        final String commandName = "example-command-api.notification-added";
        final Optional<Long> version = empty();
        final UUID streamId = randomUUID();
        final UUID envelopeId = randomUUID();

        final JsonEnvelope jsonEnvelope = anEnvelope(commandName, version, envelopeId, streamId);

        try {
            streamStatusFactory.create(jsonEnvelope, streamId);
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
    private JsonEnvelope anEnvelope(final String commandName, final Optional<Long> version, final UUID envelopeId, final UUID streamId) {

        final MetadataBuilder metadataBuilder = metadataBuilder()
                .withId(envelopeId)
                .withName(commandName)
                .withStreamId(streamId);

        version.ifPresent(metadataBuilder::withVersion);

        return envelopeFrom(
                metadataBuilder,
                createObjectBuilder()
                        .add("exampleField", "example value"));
    }
}
