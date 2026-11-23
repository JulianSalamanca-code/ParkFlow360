-- Modelo multiempresa: roles PLATAFORMA/EMPRESA/CAJERO_PAGOS/GUARDIA,
-- parqueaderos, vinculaciones y filtrado de datos por parqueadero.

-- El rol ADMIN se conserva como alias en el código (tokens vigentes); en BD
-- se normaliza para que los nuevos controles usen un único valor.
UPDATE usuarios SET rol = 'PLATAFORMA' WHERE UPPER(rol) = 'ADMIN';

CREATE TABLE IF NOT EXISTS empresas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    identificador VARCHAR(80) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS empresa_usuarios (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE (empresa_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS parqueaderos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE RESTRICT,
    nombre VARCHAR(150) NOT NULL,
    identificador VARCHAR(80) NOT NULL UNIQUE,
    capacidad_vehiculos INTEGER,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parqueaderos_tipos_vehiculo (
    id BIGSERIAL PRIMARY KEY,
    parqueadero_id BIGINT NOT NULL REFERENCES parqueaderos(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    cantidad_fija BOOLEAN NOT NULL DEFAULT FALSE,
    cantidad INTEGER,
    UNIQUE (parqueadero_id, tipo)
);

CREATE TABLE IF NOT EXISTS parqueadero_usuarios (
    id BIGSERIAL PRIMARY KEY,
    parqueadero_id BIGINT NOT NULL REFERENCES parqueaderos(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_asignado VARCHAR(30) NOT NULL
        CHECK (rol_asignado IN ('CAJERO_PAGOS', 'GUARDIA', 'USUARIO')),
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_pu_parqueadero_usuario_rol
    ON parqueadero_usuarios (parqueadero_id, usuario_id, rol_asignado);

-- Un cajero o guardia pertenece a un único parqueadero.
CREATE UNIQUE INDEX IF NOT EXISTS uq_pu_usuario_unico_por_rol
    ON parqueadero_usuarios (usuario_id)
    WHERE rol_asignado IN ('CAJERO_PAGOS', 'GUARDIA');

ALTER TABLE espacios ADD COLUMN IF NOT EXISTS parqueadero_id BIGINT REFERENCES parqueaderos(id);
ALTER TABLE planos ADD COLUMN IF NOT EXISTS parqueadero_id BIGINT REFERENCES parqueaderos(id);
ALTER TABLE ingresos ADD COLUMN IF NOT EXISTS parqueadero_id BIGINT REFERENCES parqueaderos(id);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS parqueadero_id BIGINT REFERENCES parqueaderos(id);
ALTER TABLE tarifas ADD COLUMN IF NOT EXISTS parqueadero_id BIGINT REFERENCES parqueaderos(id);

-- Los datos existentes se asocian a un parqueadero migrado para no perder historial.
INSERT INTO empresas (nombre, identificador, activo, fecha_creacion)
SELECT 'Empresa migrada', 'EMPRESA-LEGACY', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE identificador = 'EMPRESA-LEGACY');

INSERT INTO parqueaderos (empresa_id, nombre, identificador, capacidad_vehiculos, activo, fecha_creacion)
SELECT e.id, 'Parqueadero migrado', 'PARQUEADERO-LEGACY', NULL, TRUE, CURRENT_TIMESTAMP
FROM empresas e
WHERE e.identificador = 'EMPRESA-LEGACY'
  AND NOT EXISTS (SELECT 1 FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY');

UPDATE espacios
SET parqueadero_id = (SELECT id FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY')
WHERE parqueadero_id IS NULL;

UPDATE planos
SET parqueadero_id = (SELECT id FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY')
WHERE parqueadero_id IS NULL;

UPDATE ingresos
SET parqueadero_id = (SELECT id FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY')
WHERE parqueadero_id IS NULL;

UPDATE pagos
SET parqueadero_id = (SELECT id FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY')
WHERE parqueadero_id IS NULL;

UPDATE tarifas
SET parqueadero_id = (SELECT id FROM parqueaderos WHERE identificador = 'PARQUEADERO-LEGACY')
WHERE parqueadero_id IS NULL;

-- V12 creó usuario_id como INTEGER; la entidad y el resto de claves usan BIGINT.
ALTER TABLE pagos ALTER COLUMN usuario_id TYPE BIGINT USING usuario_id::BIGINT;

-- RN-13: el numero de espacio es unico por parqueadero, no global.
DO $$
DECLARE numero_constraint text;
BEGIN
    SELECT conname INTO numero_constraint
    FROM pg_constraint
    WHERE conrelid = 'espacios'::regclass
      AND contype = 'u'
      AND conkey = ARRAY[(
          SELECT attnum FROM pg_attribute
          WHERE attrelid = 'espacios'::regclass AND attname = 'numero' AND NOT attisdropped
      )]::smallint[];

    IF numero_constraint IS NOT NULL THEN
        EXECUTE format('ALTER TABLE espacios DROP CONSTRAINT %I', numero_constraint);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_espacios_parqueadero_numero') THEN
        ALTER TABLE espacios
            ADD CONSTRAINT uq_espacios_parqueadero_numero UNIQUE (parqueadero_id, numero);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_espacios_parqueadero ON espacios(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_planos_parqueadero ON planos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_ingresos_parqueadero ON ingresos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_pagos_parqueadero ON pagos(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_tarifas_parqueadero ON tarifas(parqueadero_id);
CREATE INDEX IF NOT EXISTS idx_pu_usuario ON parqueadero_usuarios(usuario_id);
CREATE INDEX IF NOT EXISTS idx_eu_usuario ON empresa_usuarios(usuario_id);
