package com.parkflow360.shared.service;

import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccesoParqueaderoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Mock
    private ParqueaderoRepository parqueaderoRepository;
    @Mock
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;

    @InjectMocks
    private AccesoParqueaderoService service;

    @Test
    void usuarioPuedeVerTodosSusParqueaderosPeroNoUnoAjeno() {
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(1L).email("cliente@test.com").rol(Roles.USUARIO).build()));
        when(parqueaderoUsuarioRepository.findByUsuarioId(1L)).thenReturn(List.of(
                vinculo(10L, 1L, Roles.USUARIO),
                vinculo(20L, 1L, Roles.USUARIO)));

        assertEquals(List.of(10L, 20L), service.parqueaderosPermitidos("cliente@test.com"));
        assertDoesNotThrow(() -> service.verificarAcceso("cliente@test.com", 20L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.verificarAcceso("cliente@test.com", 99L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cajeroYGardiaSoloVenSuParqueaderoAsignado() {
        when(usuarioRepository.findByEmail("cajero@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(2L).rol(Roles.CAJERO_PAGOS).build()));
        when(parqueaderoUsuarioRepository.findByUsuarioId(2L)).thenReturn(List.of(
                vinculo(30L, 2L, Roles.CAJERO_PAGOS)));

        when(usuarioRepository.findByEmail("guardia@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(3L).rol(Roles.GUARDIA).build()));
        when(parqueaderoUsuarioRepository.findByUsuarioId(3L)).thenReturn(List.of(
                vinculo(40L, 3L, Roles.GUARDIA)));

        assertEquals(List.of(30L), service.parqueaderosPermitidos("cajero@test.com"));
        assertEquals(List.of(40L), service.parqueaderosPermitidos("guardia@test.com"));

        assertThrows(ResponseStatusException.class,
                () -> service.verificarAcceso("cajero@test.com", 40L));
        assertThrows(ResponseStatusException.class,
                () -> service.verificarAcceso("guardia@test.com", 30L));
    }

    @Test
    void empresaVeLosParqueaderosDeSusEmpresas() {
        when(usuarioRepository.findByEmail("empresa@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(4L).rol(Roles.EMPRESA).build()));
        when(empresaUsuarioRepository.findByUsuarioId(4L)).thenReturn(List.of(
                EmpresaUsuario.builder().empresaId(7L).usuarioId(4L).build(),
                EmpresaUsuario.builder().empresaId(8L).usuarioId(4L).build()));
        when(parqueaderoRepository.findByEmpresaIdIn(List.of(7L, 8L))).thenReturn(List.of(
                parqueadero(50L, 7L),
                parqueadero(60L, 8L)));

        assertEquals(List.of(50L, 60L), service.parqueaderosPermitidos("empresa@test.com"));
        assertDoesNotThrow(() -> service.verificarAcceso("empresa@test.com", 60L));
        assertThrows(ResponseStatusException.class,
                () -> service.verificarAcceso("empresa@test.com", 30L));
    }

    @Test
    void plataformaVeTodosLosParqueaderos() {
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(5L).rol(Roles.ADMIN_LEGADO).build()));
        when(parqueaderoRepository.findAll()).thenReturn(List.of(
                parqueadero(70L, 1L), parqueadero(80L, 2L)));

        assertEquals(List.of(70L, 80L), service.parqueaderosPermitidos("admin@test.com"));
        assertDoesNotThrow(() -> service.verificarAcceso("admin@test.com", 999L));
    }

    @Test
    void resolverParqueaderoUsaElUnicoVinculadoCuandoNoSeIndica() {
        when(usuarioRepository.findByEmail("cajero@test.com")).thenReturn(Optional.of(
                Usuario.builder().id(2L).rol(Roles.CAJERO_PAGOS).build()));
        when(parqueaderoUsuarioRepository.findByUsuarioId(2L)).thenReturn(List.of(
                vinculo(30L, 2L, Roles.CAJERO_PAGOS)));

        assertEquals(30L, service.resolverParqueadero("cajero@test.com", null));
        assertEquals(30L, service.resolverParqueadero("cajero@test.com", 30L));
    }

    private ParqueaderoUsuario vinculo(Long parqueaderoId, Long usuarioId, String rol) {
        return ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuarioId)
                .rolAsignado(rol)
                .build();
    }

    private Parqueadero parqueadero(Long id, Long empresaId) {
        return Parqueadero.builder().id(id).empresaId(empresaId).build();
    }
}
