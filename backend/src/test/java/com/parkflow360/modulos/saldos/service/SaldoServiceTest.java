package com.parkflow360.modulos.saldos.service;

import com.parkflow360.modulos.saldos.dto.RecargaRequest;
import com.parkflow360.modulos.saldos.dto.SaldoResponse;
import com.parkflow360.modulos.saldos.entity.MovimientoSaldo;
import com.parkflow360.modulos.saldos.repository.MovimientoSaldoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaldoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MovimientoSaldoRepository movimientoSaldoRepository;

    @InjectMocks
    private SaldoService service;

    @Test
    void recargaRegistraMovimientoConSaldoAnteriorYPosterior() {
        Usuario usuario = usuario(1L, "10.00");
        when(usuarioRepository.findByEmail("u@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoSaldoRepository.findByUsuarioIdOrderByFechaDesc(1L)).thenReturn(List.of());

        SaldoResponse response = service.recargar("u@test.com", RecargaRequest.builder()
                .valor(new BigDecimal("5.00"))
                .referencia("RECARGA-1")
                .build());

        assertThat(response.getSaldo()).isEqualByComparingTo("15.00");
        ArgumentCaptor<MovimientoSaldo> captor = ArgumentCaptor.forClass(MovimientoSaldo.class);
        verify(movimientoSaldoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("RECARGA");
        assertThat(captor.getValue().getSaldoAnterior()).isEqualByComparingTo("10.00");
        assertThat(captor.getValue().getSaldoPosterior()).isEqualByComparingTo("15.00");
        assertThat(captor.getValue().getReferencia()).isEqualTo("RECARGA-1");
    }

    @Test
    void recargaConValorCeroSeRechaza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.recargar("u@test.com", RecargaRequest.builder()
                        .valor(BigDecimal.ZERO)
                        .build()));

        assertThat(ex.getMessage()).contains("mayor a cero");
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void debitoSinSaldoSuficienteNoGeneraMovimiento() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario(1L, "2.00")));

        boolean debitado = service.debitarSiAlcanza(1L, new BigDecimal("5.00"), "FACTURA-9");

        assertThat(debitado).isFalse();
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    private Usuario usuario(Long id, String saldo) {
        return Usuario.builder()
                .id(id)
                .email("u@test.com")
                .rol("USUARIO")
                .saldo(new BigDecimal(saldo))
                .build();
    }
}
