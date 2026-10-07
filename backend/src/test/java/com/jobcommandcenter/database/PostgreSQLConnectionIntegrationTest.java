package com.jobcommandcenter.database;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@EnabledIf("isPostgresReachable")
class PostgreSQLConnectionIntegrationTest {

    static boolean isPostgresReachable() {
        try (Socket socket = new Socket("localhost", 5432)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should connect to local PostgreSQL instance and apply Flyway migration")
    void shouldConnectToPostgresAndApplyMigrations() throws SQLException {
        assertThat(dataSource).isNotNull();
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.isValid(2)).isTrue();
            assertThat(conn.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }

        assertThat(flyway).isNotNull();
        var applied = flyway.info().applied();
        assertThat(applied).isNotEmpty();
        assertThat(applied[0].getVersion().getVersion()).isEqualTo("1");

        String schemaVersion = jdbcTemplate.queryForObject(
            "SELECT metadata_value FROM system_metadata WHERE metadata_key = 'schema_version'",
            String.class
        );
        assertThat(schemaVersion).isEqualTo("1.0.0");
    }
}
