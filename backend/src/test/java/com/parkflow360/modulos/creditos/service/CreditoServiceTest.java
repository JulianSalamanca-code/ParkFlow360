package com.parkflow360.modulos.creditos.service;

import com.parkflow360.modulos.creditos.dto.CreditoRequest;
import com.parkflow360.modulos.creditos.dto.CreditoResponse;
import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditoServiceTest {

    @Mock
    private CreditoRepository creditoRepository;
    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SaldoService saldoService;
    @Mock
    private AccesoParqueaderoService accesoParqueaderoService;

    @InjectMocks
    private CreditoService service;

    @Test
    void creditoCubreSoloElFaltanteYPagaLaFactura() {
        Factura factura = factura();
        when(accesoParqueaderoService.requireUsuario("u@test.com")).thenReturn(usuario());
        when(facturaRepository.findById(9L)).thenReturn(Optional.of(factura));
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso()));
        when(creditoRepository.existsByUsuarioIdAndEstado(1L, "PENDIENTE")).thenReturn(false);
        when(saldoService.saldoBloqueado(1L)).thenReturn(new BigDecimal("500.00"));
        when(saldoService.debitarHasta(eq(1L), any(BigDecimal.class), eq("FACTURA-9")))
                .thenReturn(new BigDecimal("500.00"));
        when(creditoRepository.save(any(Credito.class))).thenAnswer(invocation -> {
            Credito credito = invocation.getArgument(0);
            credito.setId(3L);
            return credito;
        });
        when(pagoRepository.existsByIngresoIdAndEstadoPago(5L, "CONFIRMADO")).thenReturn(false);

        CreditoResponse response = service.otorgar("u@test.com",
                CreditoRequest.builder().facturaId(9L).build());

        assertThat(response.getMonto()).isEqualByComparingTo("1500.00");
        assertThat(factura.getEstado()).isEqualTo("PAGADA");
        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertThat(pago.getValue().getMetodoPago()).isEqualTo("SALDO_CREDITO");
        assertThat(pago.getValue().getValor()).isEqualByComparingTo("2000.00");
        verify(facturaRepository).save(any(Factura.class));
    }

    @Test
    void noSePuedenTenerDosCreditosPendientes() {
        when(accesoParqueaderoService.requireUsuario("u@test.com")).thenReturn(usuario());
        when(facturaRepository.findById(9L)).thenReturn(Optional.of(factura()));
        when(ingresoRepository.findById(5L)).thenReturn(Optional.of(ingreso()));
        when(creditoRepository.existsByUsuarioIdAndEstado(1L, "PENDIENTE")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.otorgar("u@test.com", CreditoRequest.builder().facturaId(9L).build()));

        assertThat(ex.getMessage()).contains("crédito pendiente");
        verify(creditoRepository, never()).save(any(Credito.class));
    }

    @Test
    void deudaVencidaSeMarcaYBloqueaConForbidden() {
        Credito vencido = Credito.builder()
                .id(3L).usuarioId(1L).facturaId(9L)
                .monto(new BigDecimal("1500.00")).montoPendiente(new BigDecimal("1500.00"))
                .fechaLimite(LocalDateTime.now().minusDays(1))
                .estado("PENDIENTE")
                .build();
        when(creditoRepository.findByUsuarioIdAndEstadoAndFechaLimiteBefore(
                eq(1L), eq("PENDIENTE"), any(LocalDateTime.class)))
                .thenReturn(List.of(vencido));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validarSinDeudaVencida(1L));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
        assertThat(ex.getReason()).contains("deuda vencida");
        assertThat(vencido.getEstado()).isEqualTo("VENCIDO");
        verify(creditoRepository).save(vencido);
    }

    private Usuario usuario() {
        return Usuario.builder().id(1L).email("u@test.com").rol("USUARIO").build();
    }

    private Factura factura() {
        return Factura.builder()
                .id(9L).ingresoId(5L).usuarioId(1L)
                .valor(new BigDecimal("2000.00")).estado("PENDIENTE")
                .build();
    }

    private Ingreso ingreso() {
        return Ingreso.builder()
                .id(5L).folio("TK-000005").placa("ABC123").categoria("CARRO")
                .parqueaderoId(10L).usuarioId(1L).estado("FINALIZADO")
                .build();
    }
}
