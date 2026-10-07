-- ============================================================================
-- Limpieza total de datos de demostración.
-- El sistema queda VACÍO y solo conserva el usuario administrador.
-- Usuario:    admin@parkflow360.com
-- Contraseña: admin123
-- ============================================================================

-- Se borran primero las tablas dependientes (pagos referencia a vehículos,
-- espacios y tarifas).
DELETE FROM ingresos;
DELETE FROM pagos;
DELETE FROM vehiculos;
DELETE FROM espacios;
DELETE FROM tarifas;
DELETE FROM planos;
DELETE FROM usuarios;

-- Reiniciar secuencias para que los nuevos registros empiecen desde 1
ALTER SEQUENCE IF EXISTS ingresos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS pagos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS vehiculos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS espacios_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tarifas_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS planos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS usuarios_id_seq RESTART WITH 1;

-- Único registro del sistema: el administrador
INSERT INTO usuarios (email, password, rol, nombre, proveedor, fecha_creacion)
VALUES (
    'admin@parkflow360.com',
    '$2b$10$unss71Ore5uMMQzKNKL9hurPpIoTmqUXO6YEAqxK2OnuKnp4R7H76',
    'ADMIN',
    'Administrador',
    'LOCAL',
    CURRENT_TIMESTAMP
);
