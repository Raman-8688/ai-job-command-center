package com.jobcommandcenter.database;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseFlywayIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("DataSource should connect successfully")
    void dataSourceShouldConnect() throws SQLException {
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
        }
    }

    @Test
    @DisplayName("Flyway migrations should execute and apply V1 baseline migration")
    void flywayShouldApplyBaselineMigration() {
        assertThat(flyway).isNotNull();
        var appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).isNotEmpty();
        assertThat(appliedMigrations[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(appliedMigrations[0].getDescription()).isEqualTo("baseline");
    }

    @Test
    @DisplayName("system_metadata infrastructure table should be initialized by Flyway")
    void systemMetadataTableShouldContainBaselineRecords() {
        String schemaVersion = jdbcTemplate.queryForObject(
            "SELECT metadata_value FROM system_metadata WHERE metadata_key = 'schema_version'",
            String.class
        );
        assertThat(schemaVersion).isEqualTo("1.0.0");

        String phase = jdbcTemplate.queryForObject(
            "SELECT metadata_value FROM system_metadata WHERE metadata_key = 'phase'",
            String.class
        );
        assertThat(phase).isEqualTo("1_backend_foundation");
    }

    @Test
    @DisplayName("Flyway should apply all migrations up to V11, create assessment tables, and apply analytics indexes")
    void flywayShouldApplyV11AndCreateAssessmentTablesAndAnalyticsIndexes() {
        var appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).isNotEmpty();
        assertThat(appliedMigrations[appliedMigrations.length - 1].getVersion().getVersion()).isEqualTo("11");
        assertThat(appliedMigrations[appliedMigrations.length - 1].getDescription()).isEqualTo("analytics indexes");

        Integer assessmentTableCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM information_schema.tables WHERE table_name = 'online_assessments' AND table_schema = 'public'",
            Integer.class
        );
        assertThat(assessmentTableCount).isEqualTo(1);

        Integer dossierTableCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM information_schema.tables WHERE table_name = 'company_dossiers' AND table_schema = 'public'",
            Integer.class
        );
        assertThat(dossierTableCount).isEqualTo(1);
    }
}
