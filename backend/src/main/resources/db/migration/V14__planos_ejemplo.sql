-- ============================================================================
-- Planos de ejemplo para demostrar el mapa de ocupación.
--
-- Nivel 1: 5 filas x 10 columnas = 50 espacios para carros.
-- Nivel 2: 4 filas x 6 columnas = 24 espacios para motos.
--
-- La imagen es solo el fondo visual; los espacios se generan con esta cuadrícula.
-- Si no los necesitas, se pueden borrar desde la aplicación
-- (Plano y Ocupación > eliminar): no tienen ingresos ni pagos asociados.
-- ============================================================================

-- --- Planos ---
INSERT INTO planos (nombre, descripcion, imagen_url, piso, filas, columnas, fecha_creacion)
SELECT 'Nivel 1 - Carros', 'Sector de carros del nivel 1', '/assets/planos/nivel1.svg', 1, 5, 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM planos WHERE nombre = 'Nivel 1 - Carros');

INSERT INTO planos (nombre, descripcion, imagen_url, piso, filas, columnas, fecha_creacion)
SELECT 'Nivel 2 - Motos', 'Sector de motos del nivel 2', '/assets/planos/nivel2.svg', 2, 4, 6, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM planos WHERE nombre = 'Nivel 2 - Motos');

-- --- Espacios del Nivel 1 (50) ---
INSERT INTO espacios (numero, tipo, estado, piso, plano_id, fila, columna, fecha_creacion)
SELECT 'N1-R' || f || 'C' || lpad(c::text, 2, '0'),
       'CARRO',
       'LIBRE',
       1,
       (SELECT id FROM planos WHERE nombre = 'Nivel 1 - Carros'),
       f,
       c,
       CURRENT_TIMESTAMP
FROM generate_series(1, 5) AS f
CROSS JOIN generate_series(1, 10) AS c
ON CONFLICT (numero) DO NOTHING;

-- --- Espacios del Nivel 2 (24) ---
INSERT INTO espacios (numero, tipo, estado, piso, plano_id, fila, columna, fecha_creacion)
SELECT 'N2-R' || f || 'C' || lpad(c::text, 2, '0'),
       'MOTO',
       'LIBRE',
       2,
       (SELECT id FROM planos WHERE nombre = 'Nivel 2 - Motos'),
       f,
       c,
       CURRENT_TIMESTAMP
FROM generate_series(1, 4) AS f
CROSS JOIN generate_series(1, 6) AS c
ON CONFLICT (numero) DO NOTHING;
