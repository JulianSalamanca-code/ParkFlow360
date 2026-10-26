package com.parkflow360.modulos.vehiculos.service;

import com.parkflow360.modulos.vehiculos.dto.VehiculoRequest;
import com.parkflow360.modulos.vehiculos.dto.VehiculoResponse;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private Vehiculo testVehiculo;
    private VehiculoRequest testRequest;

    @BeforeEach
    void setUp() {
        testVehiculo = Vehiculo.builder()
                .id(1L)
                .placa("ABC123")
                .tipo("CARRO")
                .color("Rojo")
                .modelo("Toyota Corolla")
                .build();

        testRequest = VehiculoRequest.builder()
                .placa("ABC123")
                .tipo("CARRO")
                .color("Rojo")
                .modelo("Toyota Corolla")
                .build();
    }

    @Test
    void crear_WithValidRequest_ShouldReturnVehiculoResponse() {
        // Given
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(testVehiculo);

        // When
        VehiculoResponse response = vehiculoService.crear(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("ABC123", response.getPlaca());
        assertEquals("CARRO", response.getTipo());
        assertEquals("Rojo", response.getColor());
        assertEquals("Toyota Corolla", response.getModelo());
        
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    void listarTodos_ShouldReturnListOfVehiculos() {
        // Given
        when(vehiculoRepository.findAll()).thenReturn(List.of(testVehiculo));

        // When
        List<VehiculoResponse> response = vehiculoService.listarTodos();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("ABC123", response.get(0).getPlaca());
        
        verify(vehiculoRepository).findAll();
    }

    @Test
    void obtenerPorId_WithExistingId_ShouldReturnVehiculoResponse() {
        // Given
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(testVehiculo));

        // When
        VehiculoResponse response = vehiculoService.obtenerPorId(1L);

        // Then
        assertNotNull(response);
        assertEquals("ABC123", response.getPlaca());
        
        verify(vehiculoRepository).findById(1L);
    }

    @Test
    void obtenerPorId_WithNonExistingId_ShouldThrowException() {
        // Given
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> vehiculoService.obtenerPorId(999L));
        verify(vehiculoRepository).findById(999L);
    }

    @Test
    void actualizar_WithExistingId_ShouldReturnUpdatedVehiculoResponse() {
        // Given
        VehiculoRequest updateRequest = VehiculoRequest.builder()
                .placa("XYZ789")
                .tipo("MOTO")
                .color("Azul")
                .modelo("Honda CBR")
                .build();
        
        Vehiculo updatedVehiculo = Vehiculo.builder()
                .id(1L)
                .placa("XYZ789")
                .tipo("MOTO")
                .color("Azul")
                .modelo("Honda CBR")
                .build();
        
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(testVehiculo));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(updatedVehiculo);

        // When
        VehiculoResponse response = vehiculoService.actualizar(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("XYZ789", response.getPlaca());
        assertEquals("MOTO", response.getTipo());
        assertEquals("Azul", response.getColor());
        assertEquals("Honda CBR", response.getModelo());
        
        verify(vehiculoRepository).findById(1L);
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    void eliminar_WithExistingId_ShouldDeleteVehiculo() {
        // Given
        when(vehiculoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehiculoRepository).deleteById(1L);

        // When
        vehiculoService.eliminar(1L);

        // Then
        verify(vehiculoRepository).existsById(1L);
        verify(vehiculoRepository).deleteById(1L);
    }

    @Test
    void eliminar_WithNonExistingId_ShouldThrowException() {
        // Given
        when(vehiculoRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> vehiculoService.eliminar(999L));
        verify(vehiculoRepository).existsById(999L);
        verify(vehiculoRepository, never()).deleteById(anyLong());
    }
}