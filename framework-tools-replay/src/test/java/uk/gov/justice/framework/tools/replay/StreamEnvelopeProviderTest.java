package uk.gov.justice.framework.tools.replay;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StreamEnvelopeProviderTest {

    @Mock
    private JdbcEventRepository jdbcEventRepository;

    @InjectMocks
    private StreamEnvelopeProvider streamEnvelopeProvider;

    @Test
    public void shouldConvertAListOfEnvelopesFromTheRepositoryToAListAndCloseTheStream() throws Exception {

        final UUID streamId = randomUUID();

        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);

        final ArrayList<Boolean> closeCheckerList = new ArrayList<>();

        final Stream<JsonEnvelope> envelopeStream = Stream.of(
                jsonEnvelope_1,
                jsonEnvelope_2,
                jsonEnvelope_3
        ).onClose(() -> closeCheckerList.add(true));

        when(jdbcEventRepository.getByStreamId(streamId)).thenReturn(
                envelopeStream
        );

        final List<JsonEnvelope> envelopes = streamEnvelopeProvider.getStreamAsList(streamId);

        assertThat(envelopes.size(), is(3));
        assertThat(envelopes.get(0), is(jsonEnvelope_1));
        assertThat(envelopes.get(1), is(jsonEnvelope_2));
        assertThat(envelopes.get(2), is(jsonEnvelope_3));

        assertThat(closeCheckerList.size(), is(1));
    }
}
