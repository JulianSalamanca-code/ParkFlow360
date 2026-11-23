package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FacturaEntradaAjusteIntegrationTest {

    private static final String EMAIL = "ajuste@test.com";
    private static final BigDecimal VALOR_HORA = new BigDecimal("2000.00");
    private static final BigDecimal VALOR_DIA = new BigDecimal("15000.00");

    @Autowired
    private IngresoService ingresoService;
    @Autowired
    private SaldoService saldoService;
    @Autowired
    private FacturaService facturaService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Autowired
    private TarifaRepository tarifaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private IngresoRepository ingresoRepository;
    @Autowired
    private FacturaRepository facturaRepository;
    @Autowired
    private PagoRepository pagoRepository;

    private Long empresaId;
    private Long parqueaderoId;
    private Long tarifaHoraId;
    private Long tarifaDiaId;
    private Long espacioId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa Ajuste")
                .identificador("EMP-AJUSTE")
                .build());
        empresaId = empresa.getId();
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresaId)
                .nombre("Parqueadero Ajuste")
                .identificador("PQ-AJUSTE")
                .build());
        parqueaderoId = parqueadero.getId();

        tarifaHoraId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Hora")
                .tipo("HORA")
                .valor(VALOR_HORA)
                .duracion("1 hora")
                .build()).getId();
        tarifaDiaId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Día")
                .tipo("DIA")
                .valor(VALOR_DIA)
                .duracion("1 día")
                .build()).getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(EMAIL)
                .rol(Roles.USUARIO)
                .nombre("Usuario Ajuste")
                .build());
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuario.getId())
                .rolAsignado(Roles.USUARIO)
                .build());

        espacioId = espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoId)
                .numero("AJ-" + System.nanoTime())
                .tipo("CARRO")
                .estado("LIBRE")
                .build()).getId();
    }

    @Test
    void salidaDentroDeLaToleranciaNoCobraDiferenciaAunqueSeCruceLaHora() {
        recargar("10000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);
        simularEstanciaDeHoras(ingresoId, 3);

        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura factura = obtenerFactura(ingresoId);
        assertThat(factura.getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        List<Pago> pagos = pagoRepository.findByIngresoId(ingresoId);
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("8000.00");
    }

    @Test
    void pagoEnPagaduriaRecalculaElValorYDebitaSoloElFaltante() {
        recargar("10000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);
        simularEstanciaDeHoras(ingresoId, 3);
        Factura facturaInicial = obtenerFactura(ingresoId);

        FacturaResponse cobro = facturaService.pagar(EMAIL, facturaInicial.getId());

        assertThat(cobro.getValorActual()).isEqualByComparingTo("6000.00");
        assertThat(cobro.getFaltante()).isEqualByComparingTo("0.00");
        assertThat(cobro.getFechaLimiteSalida()).isNotNull();
        Factura factura = obtenerFactura(ingresoId);
        assertThat(factura.getValor()).isEqualByComparingTo("6000.00");
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(pagoRepository.findByIngresoId(ingresoId).get(0).getValor())
                .isEqualByComparingTo("6000.00");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("4000.00");

        ingresoService.registrarSalida(EMAIL, ingresoId);
        assertThat(obtenerFactura(ingresoId).getValor()).isEqualByComparingTo("6000.00");
    }

    @Test
    void salidaConToleranciaVencidaSeBloqueaHastaQueSeRehabiliteElPlazo() {
        recargar("10000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);
        Factura factura = obtenerFactura(ingresoId);
        configurarTolerancia(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingresoService.registrarSalida(EMAIL, ingresoId));
        assertThat(ex.getMessage()).contains("tolerancia");

        configurarTolerancia(30);
        ingresoService.registrarSalida(EMAIL, ingresoId);
        assertThat(obtenerFactura(ingresoId).getId()).isEqualTo(factura.getId());
        assertThat(ingresoRepository.findById(ingresoId).orElseThrow().getEstado())
                .isEqualTo("FINALIZADO");
    }

    @Test
    void tarifaDiaNoSeCobraPorHorasAlPagarNiAlSalir() {
        recargar("20000.00");
        Long ingresoId = entrarConTarifa(tarifaDiaId, VALOR_DIA);
        simularEstanciaDeHoras(ingresoId, 10);
        Factura facturaInicial = obtenerFactura(ingresoId);

        FacturaResponse cobro = facturaService.pagar(EMAIL, facturaInicial.getId());

        assertThat(cobro.getValorActual()).isEqualByComparingTo(VALOR_DIA);
        assertThat(cobro.getFaltante()).isEqualByComparingTo("0.00");

        ingresoService.registrarSalida(EMAIL, ingresoId);

        assertThat(obtenerFactura(ingresoId).getValor()).isEqualByComparingTo(VALOR_DIA);
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(pagoRepository.findByIngresoId(ingresoId).get(0).getValor())
                .isEqualByComparingTo(VALOR_DIA);
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("5000.00");
    }

    @Test
    void saldoInsuficienteAlPagarConservaUnSoloPagoYPermiteReintentar() {
        recargar("3000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);
        simularEstanciaDeHoras(ingresoId, 3);
        Factura pendiente = obtenerFactura(ingresoId);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> facturaService.pagar(EMAIL, pendiente.getId()));
        assertThat(ex.getMessage()).contains("Saldo insuficiente");

        assertThat(obtenerFactura(ingresoId).getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("1000.00");

        recargar("5000.00");
        FacturaResponse cobro = facturaService.pagar(EMAIL, pendiente.getId());
        assertThat(cobro.getEstado()).isEqualTo("PAGADA");
        assertThat(obtenerFactura(ingresoId).getValor()).isEqualByComparingTo("6000.00");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("2000.00");
    }

    private void configurarTolerancia(int minutos) {
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        empresa.setToleranciaSalidaMinutos(minutos);
        empresaRepository.saveAndFlush(empresa);
    }

    private void recargar(String valor) {
        saldoService.recargar(EMAIL, RecargaRequest.builder().valor(new BigDecimal(valor)).build());
    }

    private Long entrarConTarifa(Long tarifaId, BigDecimal valor) {
        IngresoResponse reserva = ingresoService.reservar(EMAIL, IngresoRequest.builder()
                .parqueaderoId(parqueaderoId)
                .placa("AJU" + tarifaId)
                .categoria("CARRO")
                .espacioId(espacioId)
                .tarifaId(tarifaId)
                .tarifaValor(valor)
                .build());
        ingresoService.registrarLlegada(EMAIL, reserva.getId());
        assertThat(facturaRepository.findByIngresoId(reserva.getId())).isPresent();
        return reserva.getId();
    }

    private void simularEstanciaDeHoras(Long ingresoId, int horas) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId).orElseThrow();
        ingreso.setFechaLlegada(LocalDateTime.now().minusHours(horas));
        ingresoRepository.save(ingreso);
    }

    private Factura obtenerFactura(Long ingresoId) {
        return facturaRepository.findByIngresoId(ingresoId).orElseThrow();
    }
}
