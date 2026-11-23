package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoTipoVehiculo;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoTipoVehiculoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EspacioServiceIntegrationTest {

    private static final String EMAIL_CAJERO = "cajero-esp-integ@test.com";

    @Autowired
    private EspacioService espacioService;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private ParqueaderoTipoVehiculoRepository tipoVehiculoRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Parqueadero parqueadero;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa espacios")
                .identificador("EMP-ESP-INTEG")
                .build());
        parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Parqueadero espacios")
                .identificador("PQ-ESP-INTEG")
                .build());
        tipoVehiculoRepository.save(ParqueaderoTipoVehiculo.builder()
                .parqueaderoId(parqueadero.getId())
                .tipo("CARRO")
                .cantidadFija(true)
                .cantidad(2)
                .build());

        Usuario cajero = usuarioRepository.save(Usuario.builder()
                .email(EMAIL_CAJERO)
                .password("hash")
                .rol(Roles.CAJERO_PAGOS)
                .nombre("Cajero")
                .build());
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueadero.getId())
                .usuarioId(cajero.getId())
                .rolAsignado(Roles.CAJERO_PAGOS)
                .build());
    }

    @Test
    void crearEspacioValidaNumeroUnicoYCupoFijo() {
        EspacioResponse primero = espacioService.crear(EMAIL_CAJERO, request("A-01"));
        assertThat(primero.getParqueaderoId()).isEqualTo(parqueadero.getId());

        IllegalArgumentException duplicado = assertThrows(IllegalArgumentException.class,
                () -> espacioService.crear(EMAIL_CAJERO, request("a-01")));
        assertThat(duplicado.getMessage()).contains("Ya existe un espacio");

        espacioService.crear(EMAIL_CAJERO, request("A-02"));

        IllegalArgumentException sinCupo = assertThrows(IllegalArgumentException.class,
                () -> espacioService.crear(EMAIL_CAJERO, request("A-03")));
        assertThat(sinCupo.getMessage()).contains("cupo fijo");
    }

    @Test
    void importarLoteRespetaCupoFijoDelParqueadero() {
        List<EspacioRequest> lote = List.of(request("B-01"), request("B-02"));

        List<EspacioResponse> creados = espacioService.importar(EMAIL_CAJERO, lote);
        assertThat(creados).hasSize(2);

        IllegalArgumentException sinCupo = assertThrows(IllegalArgumentException.class,
                () -> espacioService.importar(EMAIL_CAJERO, List.of(request("B-03"))));
        assertThat(sinCupo.getMessage()).contains("cupo fijo");
    }

    @Test
    void restriccionDeNumeroUnicoPorParqueaderoEstaEnElEsquemaH2() {
        espacioRepository.saveAndFlush(espacio("A-01"));

        assertThrows(DataIntegrityViolationException.class,
                () -> espacioRepository.saveAndFlush(espacio("A-01")));
    }

    private EspacioRequest request(String numero) {
        return EspacioRequest.builder()
                .numero(numero)
                .tipo("CARRO")
                .estado("LIBRE")
                .piso(1)
                .build();
    }

    private Espacio espacio(String numero) {
        return Espacio.builder()
                .parqueaderoId(parqueadero.getId())
                .numero(numero)
                .tipo("CARRO")
                .estado("LIBRE")
                .piso(1)
                .build();
    }
}
