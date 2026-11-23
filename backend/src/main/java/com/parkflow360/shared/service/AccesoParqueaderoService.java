package com.parkflow360.shared.service;

import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

/** Resuelve en backend los parqueaderos que puede ver cada usuario autenticado. */
@Service
@RequiredArgsConstructor
public class AccesoParqueaderoService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaUsuarioRepository empresaUsuarioRepository;
    private final ParqueaderoRepository parqueaderoRepository;
    private final ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;

    public Usuario requireUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));
    }

    public boolean esPlataforma(String email) {
        return Roles.esPlataforma(requireUsuario(email).getRol());
    }

    public List<Long> parqueaderosPermitidos(String email) {
        Usuario usuario = requireUsuario(email);
        String rol = Roles.normalizar(usuario.getRol());

        if (Roles.PLATAFORMA.equals(rol)) {
            return parqueaderoRepository.findAll().stream()
                    .map(parqueadero -> parqueadero.getId())
                    .toList();
        }

        if (Roles.EMPRESA.equals(rol)) {
            List<Long> empresaIds = empresaUsuarioRepository.findByUsuarioId(usuario.getId()).stream()
                    .map(EmpresaUsuario::getEmpresaId)
                    .toList();
            if (empresaIds.isEmpty()) {
                return List.of();
            }
            return parqueaderoRepository.findByEmpresaIdIn(empresaIds).stream()
                    .map(parqueadero -> parqueadero.getId())
                    .toList();
        }

        return parqueaderoUsuarioRepository.findByUsuarioId(usuario.getId()).stream()
                .map(ParqueaderoUsuario::getParqueaderoId)
                .distinct()
                .toList();
    }

    public void verificarAcceso(String email, Long parqueaderoId) {
        if (parqueaderoId == null || esPlataforma(email)) {
            return;
        }
        if (!parqueaderosPermitidos(email).contains(parqueaderoId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes acceso a este parqueadero.");
        }
    }

    /** Resuelve el parqueadero de una operación; si hay uno solo vinculado, lo usa por defecto. */
    public Long resolverParqueadero(String email, Long solicitado) {
        if (solicitado != null) {
            verificarAcceso(email, solicitado);
            return solicitado;
        }

        List<Long> permitidos = parqueaderosPermitidos(email);
        if (permitidos.size() == 1) {
            return permitidos.get(0);
        }
        if (permitidos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes parqueaderos vinculados a tu cuenta.");
        }
        throw new IllegalArgumentException("Debes indicar el parqueadero de la operación.");
    }
}
