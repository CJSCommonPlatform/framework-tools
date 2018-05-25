package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonEnvelopeUtilTest {

    @Test
    public void shouldReturnSequenceIdOfJsonEnvelope() {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID("test")
                        .withVersion(10L))
                .build();

        assertThat(new JsonEnvelopeUtil().versionOf(jsonEnvelope), is(10L));
    }

    @Test
    public void shouldReturnMinusOneIfNoSequenceIdPresentInJsonEnvelope() {

        final UUID id = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "test"))
                .build();

        assertThat(new JsonEnvelopeUtil().versionOf(jsonEnvelope), is(-1L));
    }
}