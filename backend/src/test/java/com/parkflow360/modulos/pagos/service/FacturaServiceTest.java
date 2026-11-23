package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.dto.FacturaResponse;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private TarifaRepository tarifaRepository;
    @Mock
    private CreditoRepository creditoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SaldoService saldoService;
    @Mock
    private AccesoParqueaderoService accesoParqueaderoService;

    @InjectMocks
    private FacturaService service;

    @Test
    void tarifaHoraCobraHorasDesdeFechaEntradaYCreaPagoConSaldo() {
        Ingreso ingreso = ingreso();
        when(facturaRepository.findByIngresoId(5L)).thenReturn(Optional.empty());
        when(tarifaRepository.findById(7L)).thenReturn(Optional.of(Tarifa.builder()
                .id(7L).tipo("HORA").valor(new BigDecimal("2000.00")).build()));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura factura = invocation.getArgument(0);
            if (factura.getId() == null) {
                factura.setId(9L);
            }
            return factura;
        });
        when(pagoRepository.existsByIngresoIdAndEstadoPago(5L, "CONFIRMADO")).thenReturn(false);
        when(saldoService.debitarSiAlcanza(eq(1L), any(BigDecimal.class), eq("FACTURA-9")))
                .thenReturn(true);
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso));
        when(creditoRepository.findByFacturaId(9L)).thenReturn(Optional.empty());

        FacturaResponse response = service.generarYCobrar(ingreso);

        assertThat(response.getValor()).isEqualByComparingTo("4000.00");
        assertThat(response.getEstado()).isEqualTo("PAGADA");
        ArgumentCaptor<BigDecimal> valor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(saldoService).debitarSiAlcanza(eq(1L), valor.capture(), eq("FACTURA-9"));
        assertThat(valor.getValue()).isEqualByComparingTo("4000.00");
        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertThat(pago.getValue().getMetodoPago()).isEqualTo("SALDO");
        assertThat(pago.getValue().getEstadoPago()).isEqualTo("CONFIRMADO");
    }

    @Test
    void saldoInsuficienteDejaFacturaPendienteSinCrearPago() {
        Ingreso ingreso = ingreso();
        when(facturaRepository.findByIngresoId(5L)).thenReturn(Optional.empty());
        when(tarifaRepository.findById(7L)).thenReturn(Optional.of(Tarifa.builder()
                .id(7L).tipo("HORA").valor(new BigDecimal("2000.00")).build()));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura factura = invocation.getArgument(0);
            if (factura.getId() == null) {
                factura.setId(9L);
            }
            return factura;
        });
        when(pagoRepository.existsByIngresoIdAndEstadoPago(5L, "CONFIRMADO")).thenReturn(false);
        when(saldoService.debitarSiAlcanza(eq(1L), any(BigDecimal.class), anyString()))
                .thenReturn(false);
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso));
        when(creditoRepository.findByFacturaId(9L)).thenReturn(Optional.empty());

        FacturaResponse response = service.generarYCobrar(ingreso);

        assertThat(response.getEstado()).isEqualTo("PENDIENTE");
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void existeFacturaPorIngresoNoLaGeneraDosVeces() {
        Ingreso ingreso = ingreso();
        Factura existente = Factura.builder()
                .id(9L).ingresoId(5L).usuarioId(1L).valor(new BigDecimal("2000.00"))
                .estado("PAGADA").build();
        when(facturaRepository.findByIngresoId(5L)).thenReturn(Optional.of(existente));
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso));
        when(creditoRepository.findByFacturaId(9L)).thenReturn(Optional.empty());

        FacturaResponse response = service.generarYCobrar(ingreso);

        assertThat(response.getId()).isEqualTo(9L);
        verify(facturaRepository, never()).save(any(Factura.class));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    private Ingreso ingreso() {
        LocalDateTime ahora = LocalDateTime.now();
        return Ingreso.builder()
                .id(5L)
                .folio("TK-000005")
                .placa("ABC123")
                .categoria("CARRO")
                .estado("FINALIZADO")
                .parqueaderoId(10L)
                .usuarioId(1L)
                .tarifaId(7L)
                .tarifaValor(new BigDecimal("2000.00"))
                .fechaEntrada(ahora.minusHours(2))
                .fechaSalida(ahora)
                .build();
    }
}
