-- Soporte para inicio de sesión con Google (Google Identity Services).
-- Las cuentas de Google no tienen contraseña local.

ALTER TABLE usuarios ALTER COLUMN password DROP NOT NULL;

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS proveedor VARCHAR(20) DEFAULT 'LOCAL';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS google_id VARCHAR(100);

UPDATE usuarios SET proveedor = 'LOCAL' WHERE proveedor IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_google_id
    ON usuarios(google_id) WHERE google_id IS NOT NULL;
