package uk.gov.justice.framework.tools.replay.database;


import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiquibaseRunner {

    public void createEventStoreSchema(final DataSource eventStoreDataSource) {
        createSchema(eventStoreDataSource,
                "liquibase/event-store-db-changelog.xml",
                "liquibase/snapshot-store-db-changelog.xml");
    }

    public void createViewStoreSchema(final DataSource viewStoreDataSource) {
        createSchema(viewStoreDataSource,
                "liquibase/viewstore-db-changelog.xml",
                "liquibase/event-buffer-changelog.xml",
                "liquibase/snapshot-store-db-changelog.xml");
    }

    private void createSchema(final DataSource dataSource, final String... liquibaseChangeLogXmls) {
        boolean dropped = false;

        try {
            final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());
            for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
                final Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                        new ClassLoaderResourceAccessor(), jdbcConnection);
                if (!dropped) {
                    liquibase.dropAll();
                    dropped = true;
                }
                liquibase.update("");
            }
        } catch (final LiquibaseException | SQLException e) {
            throw new RuntimeException("Failed to initiate Liquibase", e);
        }
    }
}
