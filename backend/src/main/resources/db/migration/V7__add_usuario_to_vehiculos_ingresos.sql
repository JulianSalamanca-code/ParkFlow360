-- Asociar vehículos e ingresos a un usuario (dueño/cliente)
ALTER TABLE vehiculos ADD COLUMN IF NOT EXISTS usuario_id BIGINT;
ALTER TABLE ingresos ADD COLUMN IF NOT EXISTS usuario_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_vehiculos_usuario ON vehiculos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ingresos_usuario ON ingresos(usuario_id);
