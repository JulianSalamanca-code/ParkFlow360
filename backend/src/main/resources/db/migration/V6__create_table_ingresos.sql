-- Tabla de ingresos (registro de entrada de vehículos / tickets)
CREATE TABLE IF NOT EXISTS ingresos (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(30) NOT NULL UNIQUE,
    placa VARCHAR(20) NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    vehiculo_id BIGINT,
    espacio_id BIGINT,
    espacio_numero VARCHAR(20),
    tarifa_id BIGINT,
    tarifa_valor DECIMAL(10, 2),
    modalidad VARCHAR(30),
    operador VARCHAR(100),
    novedad VARCHAR(200),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_salida TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ingresos_placa ON ingresos(placa);
CREATE INDEX IF NOT EXISTS idx_ingresos_estado ON ingresos(estado);
CREATE INDEX IF NOT EXISTS idx_ingresos_fecha ON ingresos(fecha_entrada);
