package uk.gov.justice.framework.tools.replay.database;


import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class DatasourceCreator {

    private static final String EVENT_STORE_URL = "jdbc:h2:tcp://localhost:8092/mem:eventstore;MVCC=true";
    private static final String VIEW_STORE_URL = "jdbc:h2:tcp://localhost:8092/mem:viewstore;MVCC=true";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "sa";

    private static final String DRIVER_CLASS = "org.h2.Driver";

    public DataSource createEventStoreDataSource() {
        return initDataSource(
                EVENT_STORE_URL,
                USERNAME,
                PASSWORD);
    }

    public DataSource createViewStoreDataSource() {
        return initDataSource(
                VIEW_STORE_URL,
                USERNAME,
                PASSWORD);
    }


    @SuppressWarnings("SameParameterValue")
    private DataSource initDataSource(final String url,
                                      final String username,
                                      final String password) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(DRIVER_CLASS);

        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
