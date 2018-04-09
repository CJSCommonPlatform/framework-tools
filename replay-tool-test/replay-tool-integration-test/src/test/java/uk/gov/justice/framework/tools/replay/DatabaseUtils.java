package uk.gov.justice.framework.tools.replay;


import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.framework.tools.entity.Document;
import uk.gov.justice.framework.tools.entity.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseUtils {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");


    public static DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url",
                "db.eventstore.userName",
                "db.eventstore.password",
                "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    public static DataSource initViewStoreDb() throws Exception {
        return initDatabase("db.viewstore.url",
                "db.eventstore.userName",
                "db.eventstore.password",
                "liquibase/viewstore-db-changelog.xml", "liquibase/event-buffer-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(TEST_PROPERTIES.value("db.driver"));

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }

    public static List<String> viewStoreEvents(DataSource viewStoreDataSource) throws SQLException {
        List<String> viewStoreEvents = new LinkedList<>();
        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM test")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                viewStoreEvents.add(rs.getString("stream_id"));
            }
            return viewStoreEvents;
        }
    }

    public static List<Test> getTestEntitiesFrom(final DataSource viewStoreDataSource) throws SQLException {
        final List<Test> tests = new LinkedList<>();

        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM test")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String testId = rs.getString("test_id");
                String data = rs.getString("data");

                tests.add(new Test(UUID.fromString(testId), data));
            }
            return tests;
        }
    }

    public static int getTestCount(final DataSource viewStoreDataSource) throws SQLException {
        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT count (*) FROM test")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static int getDocumentsCount(final DataSource viewStoreDataSource) throws SQLException {
        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT count (*) FROM document")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static List<Document> getDocuments(final DataSource viewStoreDataSource) throws SQLException {

        final List<Document> documents = new ArrayList<>();

        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("SELECT * FROM document")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String document_id = rs.getString("document_id");
                final String name = rs.getString("name");
                final String testId = rs.getString("test_id");

                documents.add(new Document(UUID.fromString(document_id), name, UUID.fromString(testId)));
            }
        }
        return documents;
    }

    public static void cleanupDataSource(DataSource dataSource, String tableName) throws SQLException {
        final PreparedStatement viewStorePreparedStatement = dataSource.getConnection().prepareStatement(format("delete from %s", tableName));
        viewStorePreparedStatement.executeUpdate();
        viewStorePreparedStatement.getConnection().close();
    }
}
