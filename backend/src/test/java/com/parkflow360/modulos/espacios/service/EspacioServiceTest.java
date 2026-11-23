package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.parqueaderos.service.ParqueaderoService;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {

    @Mock
    private EspacioRepository espacioRepository;
    @Mock
    private IngresoRepository ingresoRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private AccesoParqueaderoService accesoParqueaderoService;
    @Mock
    private ParqueaderoService parqueaderoService;

    @InjectMocks
    private EspacioService service;

    @Test
    void crearEspacioAsignaParqueaderoResuelto() {
        when(accesoParqueaderoService.resolverParqueadero("cajero@test.com", null)).thenReturn(10L);
        when(espacioRepository.existsByParqueaderoIdAndNumeroIgnoreCase(10L, "A-01")).thenReturn(false);
        when(espacioRepository.save(any(Espacio.class))).thenAnswer(invocation -> {
            Espacio guardado = invocation.getArgument(0);
            assertEquals("A-01", guardado.getNumero());
            guardado.setId(1L);
            return guardado;
        });

        EspacioResponse response = service.crear("cajero@test.com", EspacioRequest.builder()
                .numero("A-01").tipo("CARRO").estado("LIBRE").piso(1).build());

        assertEquals(10L, response.getParqueaderoId());
        assertEquals("A-01", response.getNumero());
        verify(parqueaderoService).validarCupo(10L, "CARRO");
    }

    @Test
    void numeroDuplicadoConDistintaCapitalizacionSeRechaza() {
        when(accesoParqueaderoService.resolverParqueadero("cajero@test.com", null)).thenReturn(10L);
        when(espacioRepository.existsByParqueaderoIdAndNumeroIgnoreCase(10L, "A-01")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crear("cajero@test.com", EspacioRequest.builder()
                        .numero("a-01").tipo("carro").estado("LIBRE").piso(1).build()));
        assertTrue(ex.getMessage().contains("Ya existe un espacio"));
        verify(espacioRepository, never()).save(any(Espacio.class));
    }

    @Test
    void importarRechazaNumerosRepetidosEnElLote() {
        when(accesoParqueaderoService.resolverParqueadero("cajero@test.com", 10L)).thenReturn(10L);

        List<EspacioRequest> requests = List.of(
                EspacioRequest.builder().parqueaderoId(10L).numero("A-01").tipo("CARRO").estado("LIBRE").piso(1).build(),
                EspacioRequest.builder().parqueaderoId(10L).numero("a-01").tipo("CARRO").estado("LIBRE").piso(1).build());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importar("cajero@test.com", requests));
        assertTrue(ex.getMessage().contains("repetido"));
        verify(espacioRepository, never()).saveAll(any());
    }
}
