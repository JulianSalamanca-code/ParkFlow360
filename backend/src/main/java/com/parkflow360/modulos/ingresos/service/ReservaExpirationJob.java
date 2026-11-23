package com.parkflow360.modulos.ingresos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** RN-10: expira cada minuto las reservas cuya hora de espera ya venció. */
@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReservaExpirationJob {

    private final IngresoService ingresoService;

    @Scheduled(cron = "0 * * * * *")
    public void expirarReservasVencidas() {
        ingresoService.expirarReservasVencidas(LocalDateTime.now());
    }
}
