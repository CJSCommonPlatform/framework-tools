package uk.gov.justice.framework.tools.replay.database;

import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class EventStreamJdbcRepositoryFactory {

    public EventStreamJdbcRepository create(final DataSource dataSource) {

        final EventStreamJdbcRepository eventStreamJdbcRepository = new EventStreamJdbcRepository();
        setField(eventStreamJdbcRepository, "dataSource", dataSource);
        setField(eventStreamJdbcRepository, "eventStreamJdbcRepositoryHelper", new JdbcRepositoryHelper());
        setField(eventStreamJdbcRepository, "clock", new UtcClock());

        return eventStreamJdbcRepository;
    }
}
