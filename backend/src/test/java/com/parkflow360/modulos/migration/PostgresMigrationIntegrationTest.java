package com.parkflow360.modulos.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayAplicaV19SobrePostgresReal() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas).isNotEmpty();
        MigrationInfo ultima = aplicadas[aplicadas.length - 1];
        assertThat(ultima.getVersion().getVersion()).isEqualTo("19");
        assertThat(ultima.getState()).isEqualTo(MigrationState.SUCCESS);
    }

    @Test
    void v18CreaColumnasTablasEIndicesDelHito2() {
        Integer columnasSaldo = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'usuarios' AND column_name = 'saldo'
                """, Integer.class);
        assertThat(columnasSaldo).isEqualTo(1);

        List<String> tablas = jdbcTemplate.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('movimientos_saldo', 'facturas', 'creditos')
                """, String.class);
        assertThat(tablas).containsExactlyInAnyOrder(
                "movimientos_saldo", "facturas", "creditos");

        List<String> indices = jdbcTemplate.queryForList("""
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN ('uq_facturas_ingreso', 'uq_creditos_pendientes_usuario',
                                    'uq_pagos_ingreso_confirmado')
                """, String.class);
        assertThat(indices).containsExactlyInAnyOrder(
                "uq_facturas_ingreso", "uq_creditos_pendientes_usuario", "uq_pagos_ingreso_confirmado");
    }

    @Test
    void v19CreaColumnasEIndiceDeHoraDeEspera() {
        List<String> columnas = jdbcTemplate.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'ingresos'
                  AND column_name IN ('fecha_reserva', 'fecha_limite_llegada', 'fecha_llegada')
                """, String.class);
        assertThat(columnas).containsExactlyInAnyOrder(
                "fecha_reserva", "fecha_limite_llegada", "fecha_llegada");

        List<String> indices = jdbcTemplate.queryForList("""
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname = 'idx_ingresos_estado_limite_llegada'
                """, String.class);
        assertThat(indices).containsExactly("idx_ingresos_estado_limite_llegada");
    }
}
