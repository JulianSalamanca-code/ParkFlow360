-- Restricciones e índices que JPA no genera desde las entidades.
-- Se ejecuta después de que Hibernate crea el esquema (ddl-auto=create-drop).

ALTER TABLE espacios
    ADD CONSTRAINT uq_espacios_parqueadero_numero UNIQUE (parqueadero_id, numero);

CREATE INDEX IF NOT EXISTS idx_espacios_parqueadero ON espacios(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_planos_parqueadero ON planos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_ingresos_parqueadero ON ingresos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_pagos_parqueadero ON pagos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_tarifas_parqueadero ON tarifas(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_pu_usuario ON parqueadero_usuarios(usuario_id);
CREATE INDEX IF NOT EXISTS idx_eu_usuario ON empresa_usuarios(usuario_id);

-- Hito 2: unicidad de facturas e índices de apoyo (H2 no soporta índices parciales).
ALTER TABLE facturas
    ADD CONSTRAINT uq_facturas_ingreso UNIQUE (ingreso_id);

CREATE INDEX IF NOT EXISTS idx_facturas_usuario ON facturas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_creditos_usuario ON creditos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_saldo_usuario ON movimientos_saldo(usuario_id);

-- Hito 3: consulta del job de expiración de reservas.
CREATE INDEX IF NOT EXISTS idx_ingresos_estado_limite_llegada
    ON ingresos(estado, fecha_limite_llegada);
