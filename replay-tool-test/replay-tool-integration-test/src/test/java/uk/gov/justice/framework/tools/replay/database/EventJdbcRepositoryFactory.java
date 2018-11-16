package uk.gov.justice.framework.tools.replay.database;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class EventJdbcRepositoryFactory {

    public EventJdbcRepository create(final DataSource eventStoreDataSource) {

        return new EventJdbcRepository(
                new AnsiSQLEventLogInsertionStrategy(),
                new JdbcRepositoryHelper(),
                jndiName -> eventStoreDataSource,
                "",
                getLogger(EventJdbcRepository.class)
        );
    }
}
