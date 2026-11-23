package com.parkflow360.shared.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** RN-13: resuelve la tolerancia de salida configurada por la empresa del parqueadero. */
@Service
@RequiredArgsConstructor
public class ToleranciaSalidaService {

    public static final int VALOR_POR_DEFECTO = 30;

    private final ParqueaderoRepository parqueaderoRepository;
    private final EmpresaRepository empresaRepository;

    public int obtenerMinutos(Long parqueaderoId) {
        if (parqueaderoId == null) {
            return VALOR_POR_DEFECTO;
        }
        Parqueadero parqueadero = parqueaderoRepository.findById(parqueaderoId).orElse(null);
        if (parqueadero == null || parqueadero.getEmpresaId() == null) {
            return VALOR_POR_DEFECTO;
        }
        Empresa empresa = empresaRepository.findById(parqueadero.getEmpresaId()).orElse(null);
        if (empresa == null || empresa.getToleranciaSalidaMinutos() < 0) {
            return VALOR_POR_DEFECTO;
        }
        return empresa.getToleranciaSalidaMinutos();
    }

    public LocalDateTime calcularFechaLimite(LocalDateTime fechaPago, Long parqueaderoId) {
        if (fechaPago == null) {
            return null;
        }
        return fechaPago.plusMinutes(obtenerMinutos(parqueaderoId));
    }
}
