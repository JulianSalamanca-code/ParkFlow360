package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Factura;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Hito3HoraEsperaIntegrationTest {

    private static final String EMAIL = "hito3@test.com";
    private static final BigDecimal VALOR_HORA = new BigDecimal("2000.00");

    @Autowired
    private IngresoService ingresoService;
    @Autowired
    private SaldoService saldoService;
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
                .nombre("Empresa Hito 3")
                .identificador("EMP-HITO3")
                .build());
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Parqueadero Hito 3")
                .identificador("PQ-HITO3")
                .build());
        parqueaderoId = parqueadero.getId();

        tarifaHoraId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Tarifa por hora")
                .tipo("HORA")
                .valor(VALOR_HORA)
                .duracion("1 hora")
                .build()).getId();
        tarifaDiaId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Tarifa por día")
                .tipo("DIA")
                .valor(new BigDecimal("15000.00"))
                .duracion("1 día")
                .build()).getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(EMAIL)
                .rol(Roles.USUARIO)
                .nombre("Usuario Hito 3")
                .build());
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(usuario.getId())
                .rolAsignado(Roles.USUARIO)
                .build());

        espacioId = espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoId)
                .numero("A-" + System.nanoTime())
                .tipo("CARRO")
                .estado("LIBRE")
                .build()).getId();
    }

    @Test
    void reservaFijaLimiteDeUnaHoraYModalidadDerivadaDeLaTarifa() {
        IngresoResponse reserva = ingresoService.reservar(EMAIL, request(tarifaHoraId, "POR HORAS"));

        assertThat(reserva.getEstado()).isEqualTo("RESERVADO");
        assertThat(reserva.getModalidad()).isEqualTo("POR HORAS");
        assertThat(reserva.getFechaReserva()).isNotNull();
        assertThat(reserva.getFechaLimiteLlegada()).isBetween(
                reserva.getFechaReserva().plusMinutes(59),
                reserva.getFechaReserva().plusMinutes(61));
        assertThat(espacioRepository.findById(espacioId).orElseThrow().getEstado()).isEqualTo("RESERVADO");
    }

    @Test
    void llegadaDentroDelLimiteActivaSinFactura() {
        IngresoResponse reserva = ingresoService.reservar(EMAIL, request(tarifaHoraId, "POR HORAS"));

        IngresoResponse llegada = ingresoService.registrarLlegada(EMAIL, reserva.getId());

        assertThat(llegada.getEstado()).isEqualTo("ACTIVO");
        assertThat(llegada.getFechaLlegada()).isNotNull();
        assertThat(facturaRepository.findByIngresoId(reserva.getId())).isEmpty();
        assertThat(espacioRepository.findById(espacioId).orElseThrow().getEstado()).isEqualTo("OCUPADO");
    }

    @Test
    void llegadaTardiaFacturaUnaHoraYCobraSinDuplicar() {
        saldoService.recargar(EMAIL, RecargaRequest.builder().valor(new BigDecimal("5000.00")).build());
        Long ingresoId = crearReservaVencida(tarifaHoraId);

        IngresoResponse llegada = ingresoService.registrarLlegada(EMAIL, ingresoId);

        assertThat(llegada.getEstado()).isEqualTo("ACTIVO");
        assertThat(llegada.getFechaLlegada()).isNotNull();
        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
        assertThat(saldoService.obtenerMiCuenta(EMAIL).getSaldo()).isEqualByComparingTo("3000.00");

        assertThrows(IllegalArgumentException.class, () -> ingresoService.registrarLlegada(EMAIL, ingresoId));
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
    }

    @Test
    void expiracionConTarifaHoraCancelaLiberaYFacturaUnaSolaVez() {
        saldoService.recargar(EMAIL, RecargaRequest.builder().valor(new BigDecimal("5000.00")).build());
        Long ingresoId = crearReservaVencida(tarifaHoraId);

        int expiradas = ingresoService.expirarReservasVencidas(LocalDateTime.now());

        assertThat(expiradas).isPositive();
        Ingreso cancelado = ingresoRepository.findById(ingresoId).orElseThrow();
        assertThat(cancelado.getEstado()).isEqualTo("CANCELADO");
        assertThat(cancelado.getFechaSalida()).isNotNull();
        assertThat(espacioRepository.findById(espacioId).orElseThrow().getEstado()).isEqualTo("LIBRE");
        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        assertThat(factura.getValor()).isEqualByComparingTo(VALOR_HORA);
        assertThat(factura.getEstado()).isEqualTo("PAGADA");

        assertThat(ingresoService.expirarReservasVencidas(LocalDateTime.now())).isZero();
        assertThat(facturaRepository.findByIngresoId(ingresoId)).isPresent();
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
    }

    @Test
    void expiracionConTarifaNoHorariaCancelaSinFacturar() {
        Long ingresoId = crearReservaVencida(tarifaDiaId);

        ingresoService.expirarReservasVencidas(LocalDateTime.now());

        assertThat(ingresoRepository.findById(ingresoId).orElseThrow().getEstado()).isEqualTo("CANCELADO");
        assertThat(espacioRepository.findById(espacioId).orElseThrow().getEstado()).isEqualTo("LIBRE");
        assertThat(facturaRepository.findByIngresoId(ingresoId)).isEmpty();
    }

    @Test
    void llegadaDespuesDeLaExpiracionNoGeneraSegundoCobro() {
        saldoService.recargar(EMAIL, RecargaRequest.builder().valor(new BigDecimal("5000.00")).build());
        Long ingresoId = crearReservaVencida(tarifaHoraId);
        ingresoService.expirarReservasVencidas(LocalDateTime.now());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingresoService.registrarLlegada(EMAIL, ingresoId));

        assertThat(ex.getMessage()).contains("cancelada", "nueva reserva");
        assertThat(facturaRepository.findByIngresoId(ingresoId)).isPresent();
        assertThat(pagoRepository.findByIngresoId(ingresoId)).hasSize(1);
    }

    private IngresoRequest request(Long tarifaId, String modalidad) {
        return IngresoRequest.builder()
                .parqueaderoId(parqueaderoId)
                .placa("HITO3" + tarifaId)
                .categoria("CARRO")
                .espacioId(espacioId)
                .tarifaId(tarifaId)
                .tarifaValor(tarifaId.equals(tarifaHoraId) ? VALOR_HORA : new BigDecimal("15000.00"))
                .modalidad(modalidad)
                .build();
    }

    private Long crearReservaVencida(Long tarifaId) {
        LocalDateTime ahora = LocalDateTime.now();
        Ingreso reserva = ingresoRepository.save(Ingreso.builder()
                .folio("RSV-" + System.nanoTime())
                .placa("VENC" + tarifaId)
                .categoria("CARRO")
                .parqueaderoId(parqueaderoId)
                .espacioId(espacioId)
                .tarifaId(tarifaId)
                .tarifaValor(tarifaId.equals(tarifaHoraId) ? VALOR_HORA : new BigDecimal("15000.00"))
                .modalidad(tarifaId.equals(tarifaHoraId) ? "POR HORAS" : "POR DÍA")
                .estado("RESERVADO")
                .usuarioId(usuarioRepository.findByEmail(EMAIL).orElseThrow().getId())
                .fechaReserva(ahora.minusHours(2))
                .fechaLimiteLlegada(ahora.minusHours(1))
                .build());
        espacioRepository.findById(espacioId).ifPresent(espacio -> {
            espacio.setEstado("RESERVADO");
            espacioRepository.save(espacio);
        });
        return reserva.getId();
    }
}
