package com.parkflow360.modulos.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida las migraciones reales contra PostgreSQL.
 * Requiere Docker y ejecutarse con RUN_POSTGRES_IT=true.
 */
@SpringBootTest
@ActiveProfiles("postgres")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_IT", matches = "true")
class PostgresMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void flywayAplicaV17SobrePostgresReal() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas).isNotEmpty();
        MigrationInfo ultima = aplicadas[aplicadas.length - 1];
        assertThat(ultima.getVersion().getVersion()).isEqualTo("17");
        assertThat(ultima.getState()).isEqualTo(MigrationState.SUCCESS);
    }
}
