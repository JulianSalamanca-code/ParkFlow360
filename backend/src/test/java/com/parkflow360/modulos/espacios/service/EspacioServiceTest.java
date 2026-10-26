package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {

    @Mock
    private EspacioRepository espacioRepository;

    @InjectMocks
    private EspacioService espacioService;

    private Espacio testEspacio;
    private EspacioRequest testRequest;

    @BeforeEach
    void setUp() {
        testEspacio = Espacio.builder()
                .id(1L)
                .numero("A01")
                .tipo("CARRO")
                .estado("LIBRE")
                .piso(1)
                .build();

        testRequest = EspacioRequest.builder()
                .numero("A01")
                .tipo("CARRO")
                .estado("LIBRE")
                .piso(1)
                .build();
    }

    @Test
    void crear_WithValidRequest_ShouldReturnEspacioResponse() {
        // Given
        when(espacioRepository.save(any(Espacio.class))).thenReturn(testEspacio);

        // When
        EspacioResponse response = espacioService.crear(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("A01", response.getNumero());
        assertEquals("CARRO", response.getTipo());
        assertEquals("LIBRE", response.getEstado());
        assertEquals(1, response.getPiso());
        
        verify(espacioRepository).save(any(Espacio.class));
    }

    @Test
    void listarTodos_ShouldReturnListOfEspacios() {
        // Given
        when(espacioRepository.findAll()).thenReturn(List.of(testEspacio));

        // When
        List<EspacioResponse> response = espacioService.listarTodos();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("A01", response.get(0).getNumero());
        
        verify(espacioRepository).findAll();
    }

    @Test
    void obtenerPorId_WithExistingId_ShouldReturnEspacioResponse() {
        // Given
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(testEspacio));

        // When
        EspacioResponse response = espacioService.obtenerPorId(1L);

        // Then
        assertNotNull(response);
        assertEquals("A01", response.getNumero());
        
        verify(espacioRepository).findById(1L);
    }

    @Test
    void actualizar_WithExistingId_ShouldReturnUpdatedEspacioResponse() {
        // Given
        EspacioRequest updateRequest = EspacioRequest.builder()
                .numero("B02")
                .tipo("MOTO")
                .estado("OCUPADO")
                .piso(2)
                .build();
        
        Espacio updatedEspacio = Espacio.builder()
                .id(1L)
                .numero("B02")
                .tipo("MOTO")
                .estado("OCUPADO")
                .piso(2)
                .build();
        
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(testEspacio));
        when(espacioRepository.save(any(Espacio.class))).thenReturn(updatedEspacio);

        // When
        EspacioResponse response = espacioService.actualizar(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("B02", response.getNumero());
        assertEquals("MOTO", response.getTipo());
        assertEquals("OCUPADO", response.getEstado());
        assertEquals(2, response.getPiso());
        
        verify(espacioRepository).findById(1L);
        verify(espacioRepository).save(any(Espacio.class));
    }

    @Test
    void eliminar_WithExistingId_ShouldDeleteEspacio() {
        // Given
        when(espacioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(espacioRepository).deleteById(1L);

        // When
        espacioService.eliminar(1L);

        // Then
        verify(espacioRepository).existsById(1L);
        verify(espacioRepository).deleteById(1L);
    }
}