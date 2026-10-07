package com.parkflow360.modulos.planos.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.planos.dto.GenerarEspaciosRequest;
import com.parkflow360.modulos.planos.dto.PlanoRequest;
import com.parkflow360.modulos.planos.dto.PlanoResponse;
import com.parkflow360.modulos.planos.entity.Plano;
import com.parkflow360.modulos.planos.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final EspacioRepository espacioRepository;

    @Transactional(readOnly = true)
    public List<PlanoResponse> listarTodos() {
        return planoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanoResponse obtenerPorId(Long id) {
        return mapToResponse(requirePlano(id));
    }

    @Transactional
    public PlanoResponse crear(PlanoRequest request) {
        Plano plano = Plano.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .piso(request.getPiso())
                .filas(request.getFilas() != null ? request.getFilas() : 0)
                .columnas(request.getColumnas() != null ? request.getColumnas() : 0)
                .ancho(request.getAncho())
                .alto(request.getAlto())
                .build();
        return mapToResponse(planoRepository.save(plano));
    }

    @Transactional
    public PlanoResponse actualizar(Long id, PlanoRequest request) {
        Plano plano = requirePlano(id);
        plano.setNombre(request.getNombre());
        plano.setDescripcion(request.getDescripcion());
        if (request.getImagenUrl() != null) plano.setImagenUrl(request.getImagenUrl());
        plano.setPiso(request.getPiso());
        if (request.getFilas() != null) plano.setFilas(request.getFilas());
        if (request.getColumnas() != null) plano.setColumnas(request.getColumnas());
        plano.setAncho(request.getAncho());
        plano.setAlto(request.getAlto());
        return mapToResponse(planoRepository.save(plano));
    }

    @Transactional
    public void eliminar(Long id) {
        Plano plano = requirePlano(id);
        // Elimina primero los espacios asociados para no violar la referencia
        List<Espacio> espacios = espacioRepository.findByPlanoId(id);
        if (!espacios.isEmpty()) {
            espacioRepository.deleteAll(espacios);
        }
        planoRepository.delete(plano);
    }

    /**
     * Genera en bloque los espacios de un plano a partir de una cuadrícula
     * (filas x columnas), evitando registrarlos uno por uno.
     */
    @Transactional
    public List<EspacioResponse> generarEspacios(Long planoId, GenerarEspaciosRequest request) {
        Plano plano = requirePlano(planoId);

        int filas = request.getFilas() != null && request.getFilas() > 0 ? request.getFilas() : plano.getFilas();
        int columnas = request.getColumnas() != null && request.getColumnas() > 0 ? request.getColumnas() : plano.getColumnas();
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("Debes indicar filas y columnas mayores a cero para generar los espacios.");
        }

        String prefijo = request.getPrefijo() != null ? request.getPrefijo().trim() : "";
        String estado = request.getEstadoInicial() != null && !request.getEstadoInicial().isBlank()
                ? request.getEstadoInicial().toUpperCase() : "LIBRE";
        Integer piso = request.getPiso() != null ? request.getPiso() : plano.getPiso();

        List<EspacioResponse> creados = new ArrayList<>();

        for (int f = 1; f <= filas; f++) {
            for (int c = 1; c <= columnas; c++) {
                String numero = buildNumero(prefijo, f, c);
                if (espacioRepository.existsByNumero(numero)) {
                    continue;
                }

                Espacio espacio = Espacio.builder()
                        .numero(numero)
                        .tipo(request.getTipo())
                        .estado(estado)
                        .piso(piso != null ? piso : 1)
                        .planoId(plano.getId())
                        .fila(f)
                        .columna(c)
                        .build();

                creados.add(mapEspacio(espacioRepository.save(espacio)));
            }
        }

        // Refleja la cuadrícula usada en el plano
        plano.setFilas(filas);
        plano.setColumnas(columnas);
        planoRepository.save(plano);

        return creados;
    }

    @Transactional(readOnly = true)
    public List<EspacioResponse> listarEspacios(Long planoId) {
        requirePlano(planoId);
        return espacioRepository.findByPlanoId(planoId).stream()
                .map(this::mapEspacio)
                .collect(Collectors.toList());
    }

    // ---------- Helpers ----------

    private Plano requirePlano(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plano no encontrado con id: " + id));
    }

    private String buildNumero(String prefijo, int fila, int columna) {
        String base = prefijo == null ? "" : prefijo;
        return String.format("%sR%dC%d", base, fila, columna);
    }

    private PlanoResponse mapToResponse(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nombre(plano.getNombre())
                .descripcion(plano.getDescripcion())
                .imagenUrl(plano.getImagenUrl())
                .piso(plano.getPiso())
                .filas(plano.getFilas())
                .columnas(plano.getColumnas())
                .ancho(plano.getAncho())
                .alto(plano.getAlto())
                .totalEspacios(espacioRepository.countByPlanoId(plano.getId()))
                .fechaCreacion(plano.getFechaCreacion())
                .build();
    }

    private EspacioResponse mapEspacio(Espacio e) {
        return EspacioResponse.builder()
                .id(e.getId())
                .numero(e.getNumero())
                .tipo(e.getTipo())
                .estado(e.getEstado())
                .piso(e.getPiso())
                .planoId(e.getPlanoId())
                .fila(e.getFila())
                .columna(e.getColumna())
                .posX(e.getPosX())
                .posY(e.getPosY())
                .ancho(e.getAncho())
                .alto(e.getAlto())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }
}
