package com.parkflow360.modulos.parqueaderos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoResponse;
import com.parkflow360.modulos.parqueaderos.dto.TipoVehiculoRequest;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoTipoVehiculo;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoTipoVehiculoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParqueaderoServiceTest {

    @Mock
    private ParqueaderoRepository parqueaderoRepository;
    @Mock
    private ParqueaderoTipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Mock
    private EspacioRepository espacioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccesoParqueaderoService accesoParqueaderoService;

    @InjectMocks
    private ParqueaderoService service;

    @Test
    void crearParqueaderoCreaCajeroConHashYVinculaciones() {
        when(accesoParqueaderoService.requireUsuario("admin@test.com")).thenReturn(Usuario.builder()
                .id(1L).rol(Roles.PLATAFORMA).build());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(Empresa.builder()
                .id(1L).nombre("Empresa").identificador("EMP-1").build()));
        when(parqueaderoRepository.existsByIdentificador("PQ-1")).thenReturn(false);
        when(parqueaderoRepository.save(any(Parqueadero.class))).thenAnswer(invocation -> {
            Parqueadero guardado = invocation.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });
        when(tipoVehiculoRepository.save(any(ParqueaderoTipoVehiculo.class))).thenAnswer(invocation -> {
            ParqueaderoTipoVehiculo guardado = invocation.getArgument(0);
            guardado.setId(100L);
            return guardado;
        });
        when(usuarioRepository.existsByEmail("cajero@test.com")).thenReturn(false);
        when(passwordEncoder.encode("clave1234")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario guardado = invocation.getArgument(0);
            guardado.setId(200L);
            return guardado;
        });
        when(espacioRepository.countByParqueaderoIdAndTipo(10L, "CARRO")).thenReturn(0L);

        ParqueaderoResponse response = service.crear(1L, ParqueaderoRequest.builder()
                .nombre("Parqueadero Central")
                .identificador("pq-1")
                .capacidadVehiculos(50)
                .tipos(List.of(TipoVehiculoRequest.builder()
                        .tipo("carro").cantidadFija(true).cantidad(10).build()))
                .cajeroEmail("cajero@test.com")
                .cajeroPassword("clave1234")
                .cajeroNombre("Cajero")
                .build(), "admin@test.com");

        assertEquals(10L, response.getId());
        assertEquals("PQ-1", response.getIdentificador());
        assertEquals("cajero@test.com", response.getCajeroEmail());
        assertEquals(1, response.getTipos().size());
        assertEquals("CARRO", response.getTipos().get(0).getTipo());
        assertEquals(0L, response.getTipos().get(0).getEspaciosOcupados());

        ArgumentCaptor<Usuario> cajeroCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(cajeroCaptor.capture());
        assertEquals(Roles.CAJERO_PAGOS, cajeroCaptor.getValue().getRol());
        assertEquals("hash-bcrypt", cajeroCaptor.getValue().getPassword());

        ArgumentCaptor<ParqueaderoUsuario> vinculoCaptor = ArgumentCaptor.forClass(ParqueaderoUsuario.class);
        verify(parqueaderoUsuarioRepository).save(vinculoCaptor.capture());
        assertEquals(10L, vinculoCaptor.getValue().getParqueaderoId());
        assertEquals(200L, vinculoCaptor.getValue().getUsuarioId());
        assertEquals(Roles.CAJERO_PAGOS, vinculoCaptor.getValue().getRolAsignado());
    }

    @Test
    void crearParqueaderoRechazaEmailDeCajeroDuplicado() {
        when(accesoParqueaderoService.requireUsuario("admin@test.com")).thenReturn(Usuario.builder()
                .id(1L).rol(Roles.PLATAFORMA).build());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(Empresa.builder().id(1L).build()));
        when(parqueaderoRepository.existsByIdentificador("PQ-2")).thenReturn(false);
        when(parqueaderoRepository.save(any(Parqueadero.class))).thenAnswer(invocation -> {
            Parqueadero guardado = invocation.getArgument(0);
            guardado.setId(11L);
            return guardado;
        });
        when(usuarioRepository.existsByEmail("cajero@test.com")).thenReturn(true);

        ParqueaderoRequest request = ParqueaderoRequest.builder()
                .nombre("Central")
                .identificador("pq-2")
                .cajeroEmail("cajero@test.com")
                .cajeroPassword("clave1234")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crear(1L, request, "admin@test.com"));
        assertTrue(ex.getMessage().contains("Ya existe un usuario"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cupoFijoNoSePuedeExceder() {
        when(tipoVehiculoRepository.findByParqueaderoId(10L)).thenReturn(List.of(
                ParqueaderoTipoVehiculo.builder()
                        .parqueaderoId(10L)
                        .tipo("CARRO")
                        .cantidadFija(true)
                        .cantidad(1)
                        .build()));
        when(espacioRepository.countByParqueaderoIdAndTipo(10L, "CARRO")).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarCupo(10L, "carro"));
        assertTrue(ex.getMessage().contains("cupo fijo"));
    }
}
