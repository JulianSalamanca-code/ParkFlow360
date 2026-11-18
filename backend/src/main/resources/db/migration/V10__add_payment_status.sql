ALTER TABLE pagos
    ADD COLUMN IF NOT EXISTS estado_pago VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_pagos_estado_pago'
    ) THEN
        ALTER TABLE pagos
            ADD CONSTRAINT ck_pagos_estado_pago
            CHECK (estado_pago IN ('CONFIRMADO', 'FALLIDO'));
    END IF;
END $$;
