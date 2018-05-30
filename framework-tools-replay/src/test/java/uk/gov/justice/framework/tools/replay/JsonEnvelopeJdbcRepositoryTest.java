package uk.gov.justice.framework.tools.replay;

import static java.util.Arrays.asList;
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
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private JsonEnvelopeJdbcRepository jsonEnvelopeJdbcRepository;

    @Test
    public void shouldGetAStreamOfEventsByPositionAndStreamIdAndConvertToStreamOfJsonEnvelopes() throws Exception {

        final UUID streamId = randomUUID();
        final Long position = 123L;
        final Long pageSize = 23L;

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        final Stream<Event> eventStream = of(event_1, event_2, event_3);

        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);

        when(eventJdbcRepository.forward(streamId, position, pageSize)).thenReturn(eventStream);
        when(eventConverter.envelopeOf(event_1)).thenReturn(jsonEnvelope_1);
        when(eventConverter.envelopeOf(event_2)).thenReturn(jsonEnvelope_2);
        when(eventConverter.envelopeOf(event_3)).thenReturn(jsonEnvelope_3);

        final Stream<JsonEnvelope> jsonEnvelopeStream = jsonEnvelopeJdbcRepository.forward(streamId, position, pageSize);

        final List<JsonEnvelope> jsonEnvelopes = jsonEnvelopeStream.collect(toList());

        assertThat(jsonEnvelopes.size(), is(3));

        assertThat(jsonEnvelopes.get(0), is(jsonEnvelope_1));
        assertThat(jsonEnvelopes.get(1), is(jsonEnvelope_2));
        assertThat(jsonEnvelopes.get(2), is(jsonEnvelope_3));
    }

    @Test
    public void shouldGetTheHeadOfAnEventStreamAndConvertToAJsonEnvelope() throws Exception {

        final UUID streamId = randomUUID();
        final Long pageSizeOfOne = 1L;

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        final Stream<Event> eventStream = of(event_1, event_2, event_3);

        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_3 = mock(JsonEnvelope.class);

        when(eventJdbcRepository.head(streamId, pageSizeOfOne)).thenReturn(eventStream);
        when(eventConverter.envelopeOf(event_1)).thenReturn(jsonEnvelope_1);
        when(eventConverter.envelopeOf(event_2)).thenReturn(jsonEnvelope_2);
        when(eventConverter.envelopeOf(event_3)).thenReturn(jsonEnvelope_3);

        final JsonEnvelope jsonEnvelope = jsonEnvelopeJdbcRepository.head(streamId);

        assertThat(jsonEnvelope, is(jsonEnvelope_1));
    }

    @Test
    public void shouldGetLatestSequenceIdForStreamFromTheEventJdbcRepository() throws Exception {

        final UUID streamId = randomUUID();
        final Long sequenceId = 98234L;

        when(eventJdbcRepository.getLatestSequenceIdForStream(streamId)).thenReturn(sequenceId);

        assertThat(jsonEnvelopeJdbcRepository.getLatestSequenceIdForStream(streamId), is(sequenceId));
    }
}
