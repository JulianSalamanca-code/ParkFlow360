-- Hito 3: hora de espera, llegada tardía y no-show (F4, RN-09 a RN-12).

ALTER TABLE ingresos
    ADD COLUMN IF NOT EXISTS fecha_reserva TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_limite_llegada TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_llegada TIMESTAMP;

-- Las reservas anteriores a V19 no tienen límite: se les fija desde su fecha de entrada
-- para que el job de expiración pueda procesarlas como reservas vencidas.
UPDATE ingresos
SET fecha_reserva = COALESCE(fecha_reserva, fecha_entrada),
    fecha_limite_llegada = COALESCE(fecha_limite_llegada, fecha_entrada + INTERVAL '1 hour')
WHERE estado = 'RESERVADO'
  AND fecha_entrada IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ingresos_estado_limite_llegada
    ON ingresos(estado, fecha_limite_llegada);
