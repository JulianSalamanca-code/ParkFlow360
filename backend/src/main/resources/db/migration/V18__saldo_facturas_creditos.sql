-- Hito 2: saldo recargable, facturas y créditos (F1-F3).

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS saldo NUMERIC(12,2) NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_usuarios_saldo_no_negativo') THEN
        ALTER TABLE usuarios
            ADD CONSTRAINT ck_usuarios_saldo_no_negativo CHECK (saldo >= 0);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS movimientos_saldo (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('RECARGA', 'DEBITO', 'CREDITO', 'PAGO_CREDITO')),
    valor NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    saldo_anterior NUMERIC(12,2) NOT NULL,
    saldo_posterior NUMERIC(12,2) NOT NULL,
    referencia VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movimientos_saldo_usuario
    ON movimientos_saldo(usuario_id);

CREATE TABLE IF NOT EXISTS facturas (
    id BIGSERIAL PRIMARY KEY,
    ingreso_id BIGINT NOT NULL REFERENCES ingresos(id) ON DELETE RESTRICT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    valor NUMERIC(12,2) NOT NULL CHECK (valor >= 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'PAGADA')),
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_pago TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_facturas_ingreso
    ON facturas(ingreso_id);

CREATE INDEX IF NOT EXISTS idx_facturas_usuario
    ON facturas(usuario_id);

CREATE TABLE IF NOT EXISTS creditos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    factura_id BIGINT NOT NULL REFERENCES facturas(id) ON DELETE RESTRICT,
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    monto_pendiente NUMERIC(12,2) NOT NULL CHECK (monto_pendiente >= 0),
    fecha_otorgamiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'PAGADO', 'VENCIDO'))
);

CREATE INDEX IF NOT EXISTS idx_creditos_usuario
    ON creditos(usuario_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_creditos_pendientes_usuario
    ON creditos(usuario_id)
    WHERE estado = 'PENDIENTE';

-- V15 impedía más de un pago por ingreso. Con facturas se permiten reintentos,
-- pero se mantiene la idempotencia: solo un pago CONFIRMADO por ingreso.
DROP INDEX IF EXISTS idx_pagos_ingreso_unico;

CREATE UNIQUE INDEX IF NOT EXISTS uq_pagos_ingreso_confirmado
    ON pagos (ingreso_id)
    WHERE ingreso_id IS NOT NULL AND estado_pago = 'CONFIRMADO';
