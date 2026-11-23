package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.creditos.dto.CreditoRequest;
import com.parkflow360.modulos.creditos.dto.CreditoResponse;
import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.creditos.service.CreditoService;
import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.ingresos.service.IngresoService;
import com.parkflow360.modulos.pagos.dto.FacturaResponse;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.modulos.saldos.dto.RecargaRequest;
import com.parkflow360.modulos.saldos.entity.MovimientoSaldo;
import com.parkflow360.modulos.saldos.repository.MovimientoSaldoRepository;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Hito2FacturacionIntegrationTest {

    private static final String EMAIL = "usuario-hito2@test.com";

    @Autowired
    private SaldoService saldoService;
    @Autowired
    private IngresoService ingresoService;
    @Autowired
    private FacturaService facturaService;
    @Autowired
    private CreditoService creditoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Autowired
    private TarifaRepository tarifaRepository;
    @Autowired
    private IngresoRepository ingresoRepository;
    @Autowired
    private FacturaRepository facturaRepository;
    @Autowired
    private PagoRepository pagoRepository;
    @Autowired
    private CreditoRepository creditoRepository;
    @Autowired
    private MovimientoSaldoRepository movimientoSaldoRepository;

    private Long parqueaderoId;
    private Long tarifaId;
    private Long usuarioId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa Hito 2")
                .identificador("EMP-HITO2")
                .build());
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Parqueadero Hito 2")
                .identificador("PQ-HITO2")
                .build());
        parqueaderoId = parqueadero.getId();

        tarifaId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Tarifa por hora")
                .tipo("HORA")
                .valor(new BigDecimal("2000.00"))
                .duracion("1 hora")
                .build()).getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(EMAIL)
                .rol(Roles.USUARIO)
                .nombre("Usuario Hito 2")
                .build());
        usuarioId = usuario.getId();
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuarioId)
                .rolAsignado(Roles.USUARIO)
                .build());
    }

    @Test
    void saldoSuficienteCobraLaFacturaAlRegistrarSalida() {
        saldoService.recargar(EMAIL, RecargaRequest.builder()
                .valor(new BigDecimal("5000.00")).build());
        Long ingresoId = crearIngresoActivo();

        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        assertThat(factura.getValor()).isEqualByComparingTo("2000.00");
        assertThat(facturaRepository.findByIngresoId(ingresoId)).isPresent();

        List<Pago> pagos = pagoRepository.findByIngresoId(ingresoId);
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getMetodoPago()).isEqualTo("SALDO");
        assertThat(pagos.get(0).getEstadoPago()).isEqualTo("CONFIRMADO");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("3000.00");
    }

    @Test
    void saldoInsuficienteCreditsYPosteriorAbonoConSaldo() {
        saldoService.recargar(EMAIL, RecargaRequest.builder()
                .valor(new BigDecimal("500.00")).build());
        Long ingresoId = crearIngresoActivo();

        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getEstado()).isEqualTo("PENDIENTE");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).isEmpty();
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("500.00");

        CreditoResponse credito = creditoService.otorgar(EMAIL,
                CreditoRequest.builder().facturaId(factura.getId()).build());
        assertThat(credito.getMonto()).isEqualByComparingTo("1500.00");
        assertThat(facturaRepository.findById(factura.getId()).orElseThrow().getEstado())
                .isEqualTo("PAGADA");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("0.00");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);

        saldoService.recargar(EMAIL, RecargaRequest.builder()
                .valor(new BigDecimal("1500.00")).build());
        FacturaResponse pagada = facturaService.pagar(EMAIL, factura.getId());

        assertThat(pagada.getEstado()).isEqualTo("PAGADA");
        Credito guardado = creditoRepository.findById(credito.getId()).orElseThrow();
        assertThat(guardado.getEstado()).isEqualTo("PAGADO");
        assertThat(guardado.getMontoPendiente()).isEqualByComparingTo("0.00");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("0.00");
        assertThat(movimientoSaldoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId))
                .extracting(MovimientoSaldo::getTipo)
                .contains("DEBITO", "PAGO_CREDITO");
    }

    @Test
    void deudaVencidaBloqueaNuevasReservas() {
        saldoService.recargar(EMAIL, RecargaRequest.builder()
                .valor(new BigDecimal("500.00")).build());
        Long ingresoId = crearIngresoActivo();
        ingresoService.registrarSalida(EMAIL, ingresoId);
        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        CreditoResponse credito = creditoService.otorgar(EMAIL,
                CreditoRequest.builder().facturaId(factura.getId()).build());

        Credito vencido = creditoRepository.findById(credito.getId()).orElseThrow();
        vencido.setFechaLimite(LocalDateTime.now().minusDays(1));
        creditoRepository.saveAndFlush(vencido);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ingresoService.reservar(EMAIL, IngresoRequest.builder()
                        .parqueaderoId(parqueaderoId)
                        .placa("XYZ789")
                        .categoria("CARRO")
                        .espacioId(999L)
                        .tarifaId(tarifaId)
                        .tarifaValor(new BigDecimal("2000.00"))
                        .build()));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        assertThat(ex.getReason()).contains("deuda vencida");
    }

    private Long crearIngresoActivo() {
        Ingreso ingreso = ingresoRepository.save(Ingreso.builder()
                .folio("TMP-" + System.nanoTime())
                .placa("ABC123")
                .categoria("CARRO")
                .estado("ACTIVO")
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuarioId)
                .tarifaId(tarifaId)
                .tarifaValor(new BigDecimal("2000.00"))
                .fechaEntrada(LocalDateTime.now().minusHours(1))
                .build());
        return ingreso.getId();
    }
}
