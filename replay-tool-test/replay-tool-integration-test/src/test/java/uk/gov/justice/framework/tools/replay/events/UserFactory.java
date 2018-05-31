package uk.gov.justice.framework.tools.replay.events;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFactory {

    public List<User> createSomeUsers(final int numberToCreate) {

        final List<User> users = new ArrayList<>();

        for (int i = 1; i <= numberToCreate; i++) {
            final User user = new User(
                    randomUUID(),
                    "firstName_" + i,
                    "lastName" + i);
            users.add(user);
        }

        return users;
    }

    public List<Event> convertToEvents(final List<User> users, final String eventName, final UUID streamId) {

        final EventBuilder eventBuilder = new EventBuilder();

        return range(0, users.size())
                .mapToObj(index -> eventBuilder.eventFrom(eventName, users.get(index), streamId, index + 1))
                .collect(toList());
    }
}
