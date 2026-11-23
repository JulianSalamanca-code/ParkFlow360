-- Hito F8: tolerancia de salida configurable por empresa (RN-13).
-- El valor se usa desde el momento en que el cajero confirma el pago.

ALTER TABLE empresas
    ADD COLUMN IF NOT EXISTS tolerancia_salida_minutos INTEGER NOT NULL DEFAULT 30;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_empresas_tolerancia_salida') THEN
        ALTER TABLE empresas
            ADD CONSTRAINT ck_empresas_tolerancia_salida
            CHECK (tolerancia_salida_minutos BETWEEN 0 AND 240);
    END IF;
END $$;
