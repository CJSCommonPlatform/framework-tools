package uk.gov.justice.framework.tools.replay.database;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class EventStreamJdbcRepositoryFactory {

    public EventStreamJdbcRepository create(final DataSource dataSource) {

        return new EventStreamJdbcRepository(
                new JdbcRepositoryHelper(),
                jndiName -> dataSource,
                new UtcClock(),
                "",
                getLogger(EventStreamJdbcRepository.class)
        );
    }
}
