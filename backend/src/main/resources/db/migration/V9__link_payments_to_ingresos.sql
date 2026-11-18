ALTER TABLE pagos
    ADD COLUMN IF NOT EXISTS ingreso_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pagos_ingreso'
    ) THEN
        ALTER TABLE pagos
            ADD CONSTRAINT fk_pagos_ingreso
            FOREIGN KEY (ingreso_id) REFERENCES ingresos(id) ON DELETE RESTRICT;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_pagos_ingreso_unico
    ON pagos (ingreso_id)
    WHERE ingreso_id IS NOT NULL;
