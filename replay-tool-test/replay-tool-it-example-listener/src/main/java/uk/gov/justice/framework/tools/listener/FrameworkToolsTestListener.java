package uk.gov.justice.framework.tools.listener;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.framework.tools.database.domain.User;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

@ServiceComponent(value = EVENT_LISTENER)
public class FrameworkToolsTestListener {

    private static final String INSERT_OR_UPDATE_STATEMENT =
            "INSERT INTO users (user_id, first_name, last_name) \n" +
            "VALUES (?, ?, ?)\n" +
            "ON CONFLICT (user_id) DO UPDATE \n" +
            "SET first_name = excluded.first_name, \n" +
            "last_name = excluded.last_name;";

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private Logger logger;

    @Inject
    private ViewStoreDataSourceProvider viewStoreDataSourceProvider;

    @Handles("framework.update-user")
    public void handle(final JsonEnvelope envelope) {

        final User user = fromJsonEnvelope(envelope);
        try {
            save(user);
            logger.debug("Event saved");
        } catch (SQLException e) {
            logger.error("Failed to insert user:" + user, e);
        }
    }

    private User fromJsonEnvelope(final JsonEnvelope envelope) {

        final String payload = envelope.payloadAsJsonObject().toString();

        try {
            return objectMapper.readValue(payload, User.class);
        } catch (final IOException e) {
            throw new RuntimeException(format("Failed to create User from json: '%s'", payload));
        }
    }

    private void save(final User user) throws SQLException {

        final DataSource dataSource = viewStoreDataSourceProvider.getDataSource();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OR_UPDATE_STATEMENT)) {
            preparedStatement.setObject(1, user.getUserId());
            preparedStatement.setObject(2, user.getFirstName());
            preparedStatement.setObject(3, user.getLastName());

            preparedStatement.execute();
        }
    }
}
