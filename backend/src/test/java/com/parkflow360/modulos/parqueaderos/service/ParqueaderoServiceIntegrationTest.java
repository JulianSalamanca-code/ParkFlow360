package com.parkflow360.modulos.parqueaderos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Autowired
    private ParqueaderoTipoVehiculoRepository tipoVehiculoRepository;
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
