package uk.gov.justice.framework.tools.replay;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;


@RunWith(MockitoJUnitRunner.class)
public class JsonEnvelopeJdbcRepositoryTest {

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private JsonEnvelopeJdbcRepository jsonEnvelopeJdbcRepository;


    @Test
    public void shouldGetAStreamOfEventsAPageAtATime() throws Exception {

        final UUID streamId = randomUUID();
        final long position = 23L;
        final long pageSize = 2L;

        final EventStream eventStream = mock(EventStream.class);

        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.readFrom(position)).thenReturn(Stream.of(jsonEnvelope_1, jsonEnvelope_2, jsonEnvelope_3));

        final Stream<JsonEnvelope> envelopeStream = jsonEnvelopeJdbcRepository.pageEventStream(streamId, position, pageSize);

        final List<JsonEnvelope> jsonEnvelopes = envelopeStream
                .collect(toList());

        assertThat(jsonEnvelopes.size(), is(2));

        assertThat(jsonEnvelopes.get(0), is(jsonEnvelope_1));
        assertThat(jsonEnvelopes.get(1), is(jsonEnvelope_2));
    }

    @Test
    public void shouldGetTheLatestEventFromAStream() throws Exception {

        final UUID streamId = randomUUID();
        final long currentVersion = 23L;

        final EventStream eventStream = mock(EventStream.class);
        final JsonEnvelope latestJsonEnvelope = mock(JsonEnvelope.class);

        final CloseChecker closeChecker = new CloseChecker();

        final Stream<JsonEnvelope> jsonEnvelopeStream = Stream.of(latestJsonEnvelope)
                .onClose(closeChecker::setClosed);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.getCurrentVersion()).thenReturn(currentVersion);

        when(eventStream.readFrom(currentVersion)).thenReturn(jsonEnvelopeStream);

        assertThat(jsonEnvelopeJdbcRepository.getLatestEvent(streamId), is(latestJsonEnvelope));

        assertThat(closeChecker.isClosed(), is(true));
    }

    @Test
    public void shouldThrowMissingEventStreamHeadExceptionIfTheStreamDoesNotHaveALatestVersion() throws Exception {

        final UUID streamId = fromString("53b9bc77-9ca6-4fe8-9dac-d11c829bb3b8");
        final long currentVersion = 23L;

        final EventStream eventStream = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.getCurrentVersion()).thenReturn(currentVersion);
        when(eventStream.readFrom(currentVersion)).thenReturn(Stream.empty());

        try {
            jsonEnvelopeJdbcRepository.getLatestEvent(streamId);
            fail();
        } catch (final MissingEventStreamHeadException expected) {
            assertThat(expected.getMessage(), is("Unable to retrieve head Event from stream with id '53b9bc77-9ca6-4fe8-9dac-d11c829bb3b8'"));
        }
    }

    @Test
    public void shouldGetTheCurrentVersion() throws Exception {

        final UUID streamId = randomUUID();
        final long currentVersion = 23L;

        final EventStream eventStream = mock(EventStream.class);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(eventStream.getCurrentVersion()).thenReturn(currentVersion);

        assertThat(jsonEnvelopeJdbcRepository.getCurrentVersion(streamId), is(currentVersion));
    }

    private static class CloseChecker {

        private boolean closed = false;

        public void setClosed() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
