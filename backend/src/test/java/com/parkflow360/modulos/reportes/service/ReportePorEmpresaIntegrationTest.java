package com.parkflow360.modulos.reportes.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.reportes.dto.ReporteResponse;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportePorEmpresaIntegrationTest {

    private static final String EMPRESA_A = "reportes-empresa-a@test.com";
    private static final String EMPRESA_B = "reportes-empresa-b@test.com";
    private static final String PLATAFORMA = "reportes-plataforma@test.com";

    @Autowired
    private ReporteService reporteService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private TarifaRepository tarifaRepository;
    @Autowired
    private PagoRepository pagoRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;

    private Long parqueaderoA;
    private Long parqueaderoB;

    @BeforeEach
    void setUp() {
        Empresa empresaA = empresaRepository.save(Empresa.builder()
                .nombre("Empresa Reportes A")
                .identificador("EMP-REP-A")
                .build());
        Empresa empresaB = empresaRepository.save(Empresa.builder()
                .nombre("Empresa Reportes B")
                .identificador("EMP-REP-B")
                .build());
        parqueaderoA = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresaA.getId())
                .nombre("Parqueadero Reportes A")
                .identificador("PQ-REP-A")
                .build()).getId();
        parqueaderoB = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresaB.getId())
                .nombre("Parqueadero Reportes B")
                .identificador("PQ-REP-B")
                .build()).getId();

        crearUsuarioEmpresa(EMPRESA_A, empresaA.getId());
        crearUsuarioEmpresa(EMPRESA_B, empresaB.getId());
        usuarioRepository.save(Usuario.builder()
                .email(PLATAFORMA)
                .rol(Roles.PLATAFORMA)
                .nombre("Plataforma")
                .build());

        espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoA).numero("RA-" + System.nanoTime())
                .tipo("CARRO").estado("OCUPADO").build());
        espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoB).numero("RB-" + System.nanoTime())
                .tipo("CARRO").estado("LIBRE").build());
        espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoB).numero("RB-" + System.nanoTime())
                .tipo("MOTO").estado("LIBRE").build());

        tarifaRepository.save(Tarifa.builder().parqueaderoId(parqueaderoA)
                .nombre("Hora A").tipo("HORA").valor(BigDecimal.TEN).build());
        tarifaRepository.save(Tarifa.builder().parqueaderoId(parqueaderoB)
                .nombre("Hora B").tipo("HORA").valor(BigDecimal.TEN).build());

        Long vehiculoA = vehiculoRepository.save(Vehiculo.builder()
                .placa("REPA01").tipo("CARRO").build()).getId();
        Long vehiculoB = vehiculoRepository.save(Vehiculo.builder()
                .placa("REPB01").tipo("CARRO").build()).getId();
        pagoRepository.save(Pago.builder().parqueaderoId(parqueaderoA).vehiculoId(vehiculoA)
                .valor(new BigDecimal("1000.00")).metodoPago("SALDO").build());
        pagoRepository.save(Pago.builder().parqueaderoId(parqueaderoB).vehiculoId(vehiculoB)
                .valor(new BigDecimal("2000.00")).metodoPago("EFECTIVO").build());
    }

    @Test
    void empresaSoloVeDatosDeSusParqueaderos() {
        List<ReporteResponse> general = reporteService.generarReporteGeneral(EMPRESA_A);

        assertThat(cantidad(general, "ESPACIOS")).isEqualTo(1L);
        assertThat(cantidad(general, "TARIFAS")).isEqualTo(1L);
        assertThat(cantidad(general, "PAGOS")).isEqualTo(1L);
        assertThat(cantidad(general, "VEHICULOS")).isEqualTo(1L);
        assertThat(total(general, "PAGOS")).isEqualByComparingTo("1000.00");

        List<ReporteResponse> espacios = reporteService.generarReporteEspaciosPorEstado(EMPRESA_A);
        assertThat(cantidad(espacios, "ESPACIO_OCUPADO")).isEqualTo(1L);
        assertThat(cantidad(espacios, "ESPACIO_LIBRE")).isZero();
    }

    @Test
    void plataformaVeTodosLosParqueaderos() {
        List<ReporteResponse> general = reporteService.generarReporteGeneral(PLATAFORMA);

        assertThat(cantidad(general, "ESPACIOS")).isEqualTo(3L);
        assertThat(cantidad(general, "TARIFAS")).isEqualTo(2L);
        assertThat(cantidad(general, "PAGOS")).isEqualTo(2L);
        assertThat(cantidad(general, "VEHICULOS")).isEqualTo(2L);
        assertThat(total(general, "PAGOS")).isEqualByComparingTo("3000.00");
    }

    @Test
    void ingresosPorPeriodoRespetaElFiltroDeParqueadero() {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(5);
        LocalDateTime fin = LocalDateTime.now().plusMinutes(5);

        List<ReporteResponse> ingresos =
                reporteService.generarReporteIngresosPorPeriodo(EMPRESA_B, inicio, fin);

        assertThat(ingresos).hasSize(1);
        assertThat(ingresos.get(0).getTipo()).isEqualTo("INGRESOS_EFECTIVO");
        assertThat(ingresos.get(0).getCantidad()).isEqualTo(1L);
        assertThat(ingresos.get(0).getTotal()).isEqualByComparingTo("2000.00");
    }

    private void crearUsuarioEmpresa(String email, Long empresaId) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(email)
                .rol(Roles.EMPRESA)
                .nombre(email)
                .build());
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresaId)
                .usuarioId(usuario.getId())
                .build());
    }

    private long cantidad(List<ReporteResponse> reportes, String tipo) {
        return reportes.stream()
                .filter(reporte -> tipo.equals(reporte.getTipo()))
                .findFirst()
                .orElseThrow()
                .getCantidad();
    }

    private BigDecimal total(List<ReporteResponse> reportes, String tipo) {
        return reportes.stream()
                .filter(reporte -> tipo.equals(reporte.getTipo()))
                .findFirst()
                .orElseThrow()
                .getTotal();
    }
}
