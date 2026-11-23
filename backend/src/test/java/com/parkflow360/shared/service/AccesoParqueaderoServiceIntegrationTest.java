package com.parkflow360.shared.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccesoParqueaderoServiceIntegrationTest {

    @Autowired
    private AccesoParqueaderoService accesoParqueaderoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;

    private Parqueadero parqueaderoUno;
    private Parqueadero parqueaderoDos;
    private Parqueadero parqueaderoTres;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa integración")
                .identificador("EMP-INTEG-1")
                .build());

        parqueaderoUno = guardarParqueadero(empresa.getId(), "PQ-INTEG-1");
        parqueaderoDos = guardarParqueadero(empresa.getId(), "PQ-INTEG-2");
        parqueaderoTres = guardarParqueadero(empresa.getId(), "PQ-INTEG-3");

        Usuario cliente = guardarUsuario("cliente-integ@test.com", Roles.USUARIO);
        vincular(parqueaderoUno.getId(), cliente.getId(), Roles.USUARIO);
        vincular(parqueaderoDos.getId(), cliente.getId(), Roles.USUARIO);

        Usuario cajero = guardarUsuario("cajero-integ@test.com", Roles.CAJERO_PAGOS);
        vincular(parqueaderoUno.getId(), cajero.getId(), Roles.CAJERO_PAGOS);

        Usuario guardia = guardarUsuario("guardia-integ@test.com", Roles.GUARDIA);
        vincular(parqueaderoDos.getId(), guardia.getId(), Roles.GUARDIA);

        Usuario empresaUser = guardarUsuario("empresa-integ@test.com", Roles.EMPRESA);
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresa.getId())
                .usuarioId(empresaUser.getId())
                .build());

        guardarUsuario("admin-integ@test.com", Roles.ADMIN_LEGADO);
    }

    @Test
    void usuarioVeSusParqueaderosYNoUnoAjeno() {
        List<Long> permitidos = accesoParqueaderoService
                .parqueaderosPermitidos("cliente-integ@test.com");

        assertThat(permitidos).containsExactlyInAnyOrder(
                parqueaderoUno.getId(), parqueaderoDos.getId());
        assertDoesNotThrow(() -> accesoParqueaderoService
                .verificarAcceso("cliente-integ@test.com", parqueaderoDos.getId()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accesoParqueaderoService
                        .verificarAcceso("cliente-integ@test.com", parqueaderoTres.getId()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cajeroYGardiaSoloVenSuParqueadero() {
        assertThat(accesoParqueaderoService.parqueaderosPermitidos("cajero-integ@test.com"))
                .containsExactly(parqueaderoUno.getId());
        assertThat(accesoParqueaderoService.parqueaderosPermitidos("guardia-integ@test.com"))
                .containsExactly(parqueaderoDos.getId());

        assertThrows(ResponseStatusException.class,
                () -> accesoParqueaderoService
                        .verificarAcceso("cajero-integ@test.com", parqueaderoDos.getId()));
        assertThrows(ResponseStatusException.class,
                () -> accesoParqueaderoService
                        .verificarAcceso("guardia-integ@test.com", parqueaderoUno.getId()));
    }

    @Test
    void empresaVeLosParqueaderosDeSuEmpresa() {
        assertThat(accesoParqueaderoService.parqueaderosPermitidos("empresa-integ@test.com"))
                .containsExactlyInAnyOrder(
                        parqueaderoUno.getId(), parqueaderoDos.getId(), parqueaderoTres.getId());
        assertDoesNotThrow(() -> accesoParqueaderoService
                .verificarAcceso("empresa-integ@test.com", parqueaderoTres.getId()));
    }

    @Test
    void plataformaVeTodosLosParqueaderos() {
        assertThat(accesoParqueaderoService.parqueaderosPermitidos("admin-integ@test.com"))
                .containsExactlyInAnyOrder(
                        parqueaderoUno.getId(), parqueaderoDos.getId(), parqueaderoTres.getId());
        assertDoesNotThrow(() -> accesoParqueaderoService
                .verificarAcceso("admin-integ@test.com", 99_999L));
    }

    private Parqueadero guardarParqueadero(Long empresaId, String identificador) {
        return parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresaId)
                .nombre("Parqueadero " + identificador)
                .identificador(identificador)
                .build());
    }

    private Usuario guardarUsuario(String email, String rol) {
        return usuarioRepository.save(Usuario.builder()
                .email(email)
                .password("hash")
                .rol(rol)
                .nombre(email)
                .build());
    }

    private void vincular(Long parqueaderoId, Long usuarioId, String rol) {
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuarioId)
                .rolAsignado(rol)
                .build());
    }
}
