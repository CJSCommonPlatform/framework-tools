package uk.gov.justice.framework.tools.replay.database;

import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class EventJdbcRepositoryFactory {

    public EventJdbcRepository create(final DataSource eventStoreDataSource) {

        final EventJdbcRepository eventJdbcRepository = new EventJdbcRepository();

        setField(eventJdbcRepository, "eventInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
        setField(eventJdbcRepository, "dataSource", eventStoreDataSource);
        setField(eventJdbcRepository, "jdbcRepositoryHelper", new JdbcRepositoryHelper());

        return eventJdbcRepository;
    }
}
