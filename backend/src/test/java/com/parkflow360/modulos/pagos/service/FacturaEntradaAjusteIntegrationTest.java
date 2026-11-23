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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FacturaEntradaAjusteIntegrationTest {

    private static final String EMAIL = "ajuste@test.com";
    private static final BigDecimal VALOR_HORA = new BigDecimal("2000.00");

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
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
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
                .valor(new BigDecimal("15000.00"))
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
    void salidaAjustaLaMismaFacturaYElMismoPagoConfirmado() {
        recargar("10000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);

        Factura apertura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(apertura.getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(apertura.getEstado()).isEqualTo("PAGADA");

        simularEstanciaDeTresHoras(ingresoId);
        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getValor()).isEqualByComparingTo("6000.00");
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        List<Pago> pagos = pagoRepository.findByIngresoId(ingresoId);
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getValor()).isEqualByComparingTo("6000.00");
        assertThat(pagos.get(0).getEstadoPago()).isEqualTo("CONFIRMADO");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("4000.00");
    }

    @Test
    void faltanteDelAjusteSeCobraConUnSoloPagoConfirmadoAlReintentar() {
        recargar("3000.00");
        Long ingresoId = entrarConTarifa(tarifaHoraId, VALOR_HORA);
        simularEstanciaDeTresHoras(ingresoId);

        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura pendiente = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(pendiente.getValor()).isEqualByComparingTo("6000.00");
        assertThat(pendiente.getEstado()).isEqualTo("PENDIENTE");
        List<Pago> pagos = pagoRepository.findByIngresoId(ingresoId);
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getValor()).isEqualByComparingTo("2000.00");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("1000.00");

        recargar("5000.00");
        FacturaResponse pagada = facturaService.pagar(EMAIL, pendiente.getId());

        assertThat(pagada.getEstado()).isEqualTo("PAGADA");
        assertThat(facturaRepository.findByIngresoId(ingresoId).orElseThrow().getValor())
                .isEqualByComparingTo("6000.00");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(pagoRepository.findByIngresoId(ingresoId).get(0).getValor())
                .isEqualByComparingTo("6000.00");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("2000.00");
    }

    @Test
    void tarifaNoHorariaNoDuplicaElValorAlSalir() {
        recargar("20000.00");
        Long ingresoId = entrarConTarifa(tarifaDiaId, new BigDecimal("15000.00"));

        ingresoService.registrarSalida(EMAIL, ingresoId);

        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getValor()).isEqualByComparingTo("15000.00");
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(pagoRepository.findByIngresoId(ingresoId).get(0).getValor())
                .isEqualByComparingTo("15000.00");
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("5000.00");
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

    private void simularEstanciaDeTresHoras(Long ingresoId) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId).orElseThrow();
        ingreso.setFechaLlegada(LocalDateTime.now().minusHours(3));
        ingresoRepository.save(ingreso);
    }
}
