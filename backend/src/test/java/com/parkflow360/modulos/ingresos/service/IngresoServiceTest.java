package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private TarifaRepository tarifaRepository;

    @InjectMocks
    private IngresoService ingresoService;

    private Ingreso testIngreso;
    private IngresoRequest testRequest;
    private Espacio testEspacio;
    private Tarifa testTarifa;

    @BeforeEach
    void setUp() {
        testEspacio = Espacio.builder()
                .id(1L)
                .numero("A01")
                .tipo("CARRO")
                .estado("LIBRE")
                .piso(1)
                .build();

        testTarifa = Tarifa.builder()
                .id(1L)
                .nombre("Tarifa por hora")
                .tipo("HORA")
                .valor(new BigDecimal("2000.00"))
                .duracion("1 hora")
                .build();

        testIngreso = Ingreso.builder()
                .id(1L)
                .folio("TK-20240101-001")
                .placa("ABC123")
                .categoria("CARRO")
                .espacioId(1L)
                .espacioNumero("A01")
                .tarifaId(1L)
                .tarifaValor(new BigDecimal("2000.00"))
                .modalidad("POR HORAS")
                .estado("ACTIVO")
                .fechaEntrada(LocalDateTime.now())
                .operador("admin@parkflow360.com")
                .novedad("Sin novedad")
                .build();

        testRequest = IngresoRequest.builder()
                .placa("ABC123")
                .categoria("CARRO")
                .espacioId(1L)
                .espacioNumero("A01")
                .tarifaId(1L)
                .tarifaValor(new BigDecimal("2000.00"))
                .modalidad("POR HORAS")
                .novedad("Sin novedad")
                .build();
    }

    @Test
    void crear_WithValidRequest_ShouldReturnIngresoResponse() {
        // Given
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(testEspacio));
        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(testTarifa));
        when(ingresoRepository.save(any(Ingreso.class))).thenReturn(testIngreso);

        // When
        IngresoResponse response = ingresoService.crear(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("TK-20240101-001", response.getFolio());
        assertEquals("ABC123", response.getPlaca());
        assertEquals("CARRO", response.getCategoria());
        assertEquals("ACTIVO", response.getEstado());
        
        verify(espacioRepository).findById(1L);
        verify(tarifaRepository).findById(1L);
        verify(ingresoRepository).save(any(Ingreso.class));
    }

    @Test
    void listarTodos_ShouldReturnListOfIngresos() {
        // Given
        when(ingresoRepository.findAll()).thenReturn(List.of(testIngreso));

        // When
        List<IngresoResponse> response = ingresoService.listarTodos();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("TK-20240101-001", response.get(0).getFolio());
        
        verify(ingresoRepository).findAll();
    }

    @Test
    void registrarSalida_WithActiveIngreso_ShouldReturnFinalizadoIngreso() {
        // Given
        Ingreso finalizadoIngreso = Ingreso.builder()
                .id(1L)
                .folio("TK-20240101-001")
                .placa("ABC123")
                .categoria("CARRO")
                .espacioId(1L)
                .espacioNumero("A01")
                .tarifaId(1L)
                .tarifaValor(new BigDecimal("2000.00"))
                .modalidad("POR HORAS")
                .estado("FINALIZADO")
                .fechaEntrada(LocalDateTime.now().minusHours(2))
                .fechaSalida(LocalDateTime.now())
                .operador("admin@parkflow360.com")
                .novedad("Sin novedad")
                .build();
        
        when(ingresoRepository.findById(1L)).thenReturn(Optional.of(testIngreso));
        when(ingresoRepository.save(any(Ingreso.class))).thenReturn(finalizadoIngreso);

        // When
        IngresoResponse response = ingresoService.registrarSalida(1L);

        // Then
        assertNotNull(response);
        assertEquals("FINALIZADO", response.getEstado());
        assertNotNull(response.getFechaSalida());
        
        verify(ingresoRepository).findById(1L);
        verify(ingresoRepository).save(any(Ingreso.class));
    }

    @Test
    void liberar_WithReservedIngreso_ShouldDeleteIngreso() {
        // Given
        Ingreso reservadoIngreso = Ingreso.builder()
                .id(1L)
                .folio("TK-20240101-001")
                .placa("ABC123")
                .categoria("CARRO")
                .espacioId(1L)
                .espacioNumero("A01")
                .estado("RESERVADO")
                .fechaEntrada(LocalDateTime.now())
                .build();
        
        when(ingresoRepository.findById(1L)).thenReturn(Optional.of(reservadoIngreso));
        doNothing().when(ingresoRepository).deleteById(1L);

        // When
        ingresoService.liberar(1L);

        // Then
        verify(ingresoRepository).findById(1L);
        verify(ingresoRepository).deleteById(1L);
    }

    @Test
    void eliminar_WithExistingId_ShouldDeleteIngreso() {
        // Given
        when(ingresoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ingresoRepository).deleteById(1L);

        // When
        ingresoService.eliminar(1L);

        // Then
        verify(ingresoRepository).existsById(1L);
        verify(ingresoRepository).deleteById(1L);
    }
}