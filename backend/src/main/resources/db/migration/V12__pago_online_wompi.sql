-- Pago en línea con Wompi: estado de la transacción, referencia y datos del recibo.
-- Los pagos existentes (registrados presencialmente) quedan como PAGADO.

ALTER TABLE pagos ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'PAGADO';
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS referencia VARCHAR(100);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS wompi_transaction_id VARCHAR(100);
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS moneda VARCHAR(10) DEFAULT 'COP';
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS usuario_id INTEGER;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS ingreso_id INTEGER;

UPDATE pagos SET estado = 'PAGADO' WHERE estado IS NULL;

CREATE INDEX IF NOT EXISTS idx_pagos_referencia ON pagos(referencia);
CREATE INDEX IF NOT EXISTS idx_pagos_usuario ON pagos(usuario_id);
