-- Unifica los roles del sistema a ADMIN y USUARIO (cliente).
-- Corrige la inconsistencia previa entre CLIENTE / OPERADOR / USER.
UPDATE usuarios SET rol = 'USUARIO' WHERE rol IS NULL OR UPPER(rol) IN ('CLIENTE', 'OPERADOR', 'USER');

UPDATE usuarios SET rol = UPPER(rol) WHERE rol IS NOT NULL;

-- Rol por defecto para nuevas cuentas (clientes)
ALTER TABLE usuarios ALTER COLUMN rol SET DEFAULT 'USUARIO';
