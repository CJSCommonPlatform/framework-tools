package uk.gov.justice.framework.tools.replay.h2;

import static java.lang.String.format;
import static java.lang.String.valueOf;

import java.sql.SQLException;

import org.h2.tools.Server;

public class InMemoryDatabaseRunner {

    private static final int PORT = 8092;

    public void startH2Database() {
        try {
            Server.createTcpServer("-tcpPort", valueOf(PORT), "-tcpAllowOthers").start();
        } catch (SQLException e) {
            throw new RuntimeException(format("Failed to start H2 database on port %d", PORT));
        }
    }

    public void stopH2Database() {
        try {
            Server.shutdownTcpServer("tcp://localhost:" + PORT, "", true, true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to stop H2 database");
        }
    }
}
