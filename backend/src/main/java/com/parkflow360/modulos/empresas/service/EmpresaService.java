package com.parkflow360.modulos.empresas.service;

import com.parkflow360.modulos.empresas.dto.ConfiguracionEmpresaRequest;
import com.parkflow360.modulos.empresas.dto.EmpresaRequest;
import com.parkflow360.modulos.empresas.dto.EmpresaResponse;
import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaUsuarioRepository empresaUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional
    public EmpresaResponse crear(String emailActor, EmpresaRequest request) {
        Usuario actor = accesoParqueaderoService.requireUsuario(emailActor);
        String rolActor = Roles.normalizar(actor.getRol());
        if (!Roles.esPlataforma(rolActor) && !Roles.EMPRESA.equals(rolActor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo PLATAFORMA o EMPRESA puede crear empresas.");
        }

        String identificador = request.getIdentificador().trim().toUpperCase(Locale.ROOT);
        if (empresaRepository.existsByIdentificador(identificador)) {
            throw new IllegalArgumentException("Ya existe una empresa con el identificador: " + identificador);
        }

        // Un usuario EMPRESA siempre se vincula a sí mismo; no puede elegir otro propietario.
        String emailPropietario = Roles.EMPRESA.equals(rolActor)
                ? actor.getEmail()
                : request.getUsuarioEmail().trim().toLowerCase(Locale.ROOT);
        Usuario propietario = usuarioRepository.findByEmail(emailPropietario)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe un usuario con el email: " + emailPropietario));
        String rolPropietario = Roles.normalizar(propietario.getRol());
        if (Roles.CAJERO_PAGOS.equals(rolPropietario) || Roles.GUARDIA.equals(rolPropietario)) {
            throw new IllegalArgumentException("El usuario indicado ya tiene un rol operativo asignado.");
        }
        if (!Roles.EMPRESA.equals(rolPropietario)) {
            propietario.setRol(Roles.EMPRESA);
            usuarioRepository.save(propietario);
        }

        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre(request.getNombre().trim())
                .identificador(identificador)
                .activo(true)
                .toleranciaSalidaMinutos(resolverTolerancia(request.getToleranciaSalidaMinutos()))
                .build());
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresa.getId())
                .usuarioId(propietario.getId())
                .build());

        return mapToResponse(empresa);
    }

    /** RN-13: la empresa define una única tolerancia para todos sus parqueaderos. */
    @Transactional
    public EmpresaResponse actualizarConfiguracion(
            String emailActor, Long empresaId, ConfiguracionEmpresaRequest request) {
        verificarAccesoEmpresa(emailActor, empresaId);
        Empresa empresa = requireEmpresa(empresaId);
        empresa.setToleranciaSalidaMinutos(
                resolverTolerancia(request.getToleranciaSalidaMinutos()));
        return mapToResponse(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarMias(String email) {
        Usuario usuario = accesoParqueaderoService.requireUsuario(email);
        if (Roles.esPlataforma(usuario.getRol())) {
            return empresaRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        List<Long> empresaIds = empresaUsuarioRepository.findByUsuarioId(usuario.getId()).stream()
                .map(EmpresaUsuario::getEmpresaId)
                .toList();
        if (empresaIds.isEmpty()) {
            return List.of();
        }
        return empresaRepository.findAllById(empresaIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Empresa requireEmpresa(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Empresa no encontrada con id: " + id));
    }

    private void verificarAccesoEmpresa(String emailActor, Long empresaId) {
        Usuario actor = accesoParqueaderoService.requireUsuario(emailActor);
        if (Roles.esPlataforma(actor.getRol())) {
            return;
        }
        if (!Roles.EMPRESA.equals(Roles.normalizar(actor.getRol()))
                || !empresaUsuarioRepository.existsByEmpresaIdAndUsuarioId(empresaId, actor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes acceso a esta empresa.");
        }
    }

    private int resolverTolerancia(Integer minutos) {
        return minutos == null ? 30 : minutos;
    }

    private EmpresaResponse mapToResponse(Empresa empresa) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .identificador(empresa.getIdentificador())
                .activo(empresa.isActivo())
                .toleranciaSalidaMinutos(empresa.getToleranciaSalidaMinutos())
                .fechaCreacion(empresa.getFechaCreacion())
                .build();
    }
}
