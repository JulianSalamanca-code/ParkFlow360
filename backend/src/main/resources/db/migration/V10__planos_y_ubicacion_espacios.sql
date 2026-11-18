-- Plano del parqueadero: permite cargar la imagen/SVG del plano y generar/ubicar
-- los espacios sobre él sin registrarlos uno por uno.

CREATE TABLE IF NOT EXISTS planos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    imagen_url VARCHAR(500),
    piso INTEGER,
    filas INTEGER NOT NULL DEFAULT 0,
    columnas INTEGER NOT NULL DEFAULT 0,
    ancho INTEGER,
    alto INTEGER,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ubicación y forma de cada espacio dentro de un plano
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS plano_id INTEGER REFERENCES planos(id);
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS fila INTEGER;
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS columna INTEGER;
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS pos_x DOUBLE PRECISION;
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS pos_y DOUBLE PRECISION;
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS ancho DOUBLE PRECISION;
ALTER TABLE espacios ADD COLUMN IF NOT EXISTS alto DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_espacios_plano ON espacios(plano_id);
