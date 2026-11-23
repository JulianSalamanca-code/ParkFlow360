package com.parkflow360.modulos.parqueaderos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.parqueaderos.dto.EspacioCrearRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoResponse;
import com.parkflow360.modulos.parqueaderos.dto.TipoVehiculoRequest;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoTipoVehiculoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParqueaderoServiceIntegrationTest {

    @Autowired
    private ParqueaderoService parqueaderoService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Autowired
    private ParqueaderoTipoVehiculoRepository tipoVehiculoRepository;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long empresaId;

    @BeforeEach
    void setUp() {
        empresaId = empresaRepository.save(Empresa.builder()
                .nombre("Empresa integración")
                .identificador("EMP-PQ-INTEG")
                .build()).getId();

        usuarioRepository.save(Usuario.builder()
                .email("admin-pq-integ@test.com")
                .password("hash")
                .rol(Roles.PLATAFORMA)
                .nombre("Administrador")
                .build());
    }

    @Test
    void crearParqueaderoGeneraCajeroTiposYVinculacion() {
        ParqueaderoResponse response = parqueaderoService.crear(empresaId, ParqueaderoRequest.builder()
                .nombre("Parqueadero integración")
                .identificador("pq-integ-nuevo")
                .capacidadVehiculos(40)
                .tipos(List.of(TipoVehiculoRequest.builder()
                        .tipo("carro")
                        .cantidadFija(true)
                        .cantidad(10)
                        .build()))
                .espacios(List.of(
                        EspacioCrearRequest.builder().numero("carro-001").tipo("carro").piso(1).build(),
                        EspacioCrearRequest.builder().numero("carro-002").tipo("carro").piso(1).build()))
                .cajeroEmail("cajero-pq-integ@test.com")
                .cajeroPassword("clave1234")
                .cajeroNombre("Cajero")
                .build(), "admin-pq-integ@test.com");

        assertThat(response.getId()).isNotNull();
        assertThat(response.getIdentificador()).isEqualTo("PQ-INTEG-NUEVO");
        assertThat(response.getCajeroEmail()).isEqualTo("cajero-pq-integ@test.com");
        assertThat(response.getTipos()).hasSize(1);
        assertThat(response.getTipos().get(0).getTipo()).isEqualTo("CARRO");
        assertThat(response.getTipos().get(0).getCantidad()).isEqualTo(10);

        Usuario cajero = usuarioRepository.findByEmail("cajero-pq-integ@test.com").orElseThrow();
        assertThat(cajero.getRol()).isEqualTo(Roles.CAJERO_PAGOS);
        assertTrue(passwordEncoder.matches("clave1234", cajero.getPassword()));

        List<ParqueaderoUsuario> vinculos = parqueaderoUsuarioRepository
                .findByParqueaderoId(response.getId());
        assertThat(vinculos).hasSize(1);
        assertThat(vinculos.get(0).getUsuarioId()).isEqualTo(cajero.getId());
        assertThat(vinculos.get(0).getRolAsignado()).isEqualTo(Roles.CAJERO_PAGOS);

        assertThat(tipoVehiculoRepository.findByParqueaderoId(response.getId())).hasSize(1);

        List<Espacio> espacios = espacioRepository.findByParqueaderoId(response.getId());
        assertThat(espacios).hasSize(2);
        assertThat(espacios).extracting(Espacio::getNumero)
                .containsExactlyInAnyOrder("CARRO-001", "CARRO-002");
        assertThat(espacios).allSatisfy(espacio -> {
            assertThat(espacio.getTipo()).isEqualTo("CARRO");
            assertThat(espacio.getEstado()).isEqualTo("LIBRE");
            assertThat(espacio.getPiso()).isEqualTo(1);
        });
    }

    @Test
    void empresaPuedeCrearParqueaderoYVerloEnLosSuyos() {
        Usuario empresa = usuarioRepository.save(Usuario.builder()
                .email("empresa-pq-integ@test.com")
                .password("hash")
                .rol(Roles.EMPRESA)
                .nombre("Empresa integración")
                .build());
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresaId)
                .usuarioId(empresa.getId())
                .build());

        ParqueaderoResponse response = parqueaderoService.crear(empresaId, ParqueaderoRequest.builder()
                .nombre("Parqueadero empresa integración")
                .identificador("pq-integ-empresa")
                .capacidadVehiculos(30)
                .tipos(List.of())
                .cajeroEmail("cajero-pq-empresa-integ@test.com")
                .cajeroPassword("clave1234")
                .build(), "empresa-pq-integ@test.com");

        assertThat(parqueaderoService.listarMios("empresa-pq-integ@test.com"))
                .extracting(ParqueaderoResponse::getId)
                .contains(response.getId());
    }

    @Test
    void crearParqueaderoRechazaCajeroConEmailYaRegistrado() {
        usuarioRepository.save(Usuario.builder()
                .email("cajero-pq-integ@test.com")
                .password("hash")
                .rol(Roles.USUARIO)
                .nombre("Otro usuario")
                .build());

        ParqueaderoRequest request = ParqueaderoRequest.builder()
                .nombre("Parqueadero duplicado")
                .identificador("pq-integ-duplicado")
                .cajeroEmail("cajero-pq-integ@test.com")
                .cajeroPassword("clave1234")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parqueaderoService.crear(empresaId, request, "admin-pq-integ@test.com"));
        assertThat(ex.getMessage()).contains("Ya existe un usuario");
    }
}
