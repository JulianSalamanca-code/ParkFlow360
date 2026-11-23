package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.creditos.dto.CreditoRequest;
import com.parkflow360.modulos.creditos.dto.CreditoResponse;
import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.creditos.service.CreditoService;
import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.EstadoPagoResponse;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.dto.VerificarIngresoRequest;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.service.FacturaService;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.modulos.saldos.dto.RecargaRequest;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class F7GuardiaIntegrationTest {

    private static final String USUARIO = "f7-usuario@test.com";
    private static final String GUARDIA = "f7-guardia@test.com";
    private static final String EMPRESA = "f7-empresa@test.com";
    private static final String CAJERO = "f7-cajero@test.com";
    private static final String OTRO_GUARDIA = "f7-otro-guardia@test.com";
    private static final BigDecimal VALOR_HORA = new BigDecimal("2000.00");

    @Autowired
    private IngresoService ingresoService;
    @Autowired
    private EstadoPagoService estadoPagoService;
    @Autowired
    private FacturaService facturaService;
    @Autowired
    private CreditoService creditoService;
    @Autowired
    private SaldoService saldoService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaUsuarioRepository empresaUsuarioRepository;
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
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private CreditoRepository creditoRepository;

    private Long parqueaderoId;
    private Long tarifaId;
    private Long usuarioId;
    private Long vehiculoId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa F7")
                .identificador("EMP-F7")
                .build());
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Parqueadero F7")
                .identificador("PQ-F7")
                .build());
        parqueaderoId = parqueadero.getId();

        Parqueadero otro = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Otro parqueadero F7")
                .identificador("PQ-F7-B")
                .build());

        tarifaId = tarifaRepository.save(Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre("Hora")
                .tipo("HORA")
                .valor(VALOR_HORA)
                .duracion("1 hora")
                .build()).getId();

        usuarioId = crearUsuario(USUARIO, Roles.USUARIO, parqueaderoId, Roles.USUARIO);
        crearUsuario(GUARDIA, Roles.GUARDIA, parqueaderoId, Roles.GUARDIA);
        crearUsuario(EMPRESA, Roles.EMPRESA, null, null);
        crearUsuario(CAJERO, Roles.CAJERO_PAGOS, parqueaderoId, Roles.CAJERO_PAGOS);
        crearUsuario(OTRO_GUARDIA, Roles.GUARDIA, otro.getId(), Roles.GUARDIA);
        crearUsuario("f7-plataforma@test.com", Roles.PLATAFORMA, null, null);
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresa.getId())
                .usuarioId(usuarioRepository.findByEmail(EMPRESA).orElseThrow().getId())
                .build());

        vehiculoId = vehiculoRepository.save(Vehiculo.builder()
                .placa("F7ABC12")
                .tipo("CARRO")
                .usuarioId(usuarioId)
                .build()).getId();
    }

    @Test
    void verificarCompletaCamposFaltantesSinSobrescribirLosExistentes() {
        Long ingresoId = crearIngreso("RESERVADO", "F7ABC12");

        IngresoResponse verificado = ingresoService.verificar(GUARDIA, ingresoId,
                VerificarIngresoRequest.builder()
                        .placa("f7abc12")
                        .vehiculoId(vehiculoId)
                        .espacioNumero("A-15")
                        .operador("Guardia F7")
                        .novedad("Sin novedad")
                        .build());

        Ingreso guardado = ingresoRepository.findById(ingresoId).orElseThrow();
        assertThat(verificado.getPlaca()).isEqualTo("F7ABC12");
        assertThat(guardado.getVehiculoId()).isEqualTo(vehiculoId);
        assertThat(guardado.getEspacioNumero()).isEqualTo("A-15");
        assertThat(guardado.getOperador()).isEqualTo("Guardia F7");
        assertThat(guardado.getNovedad()).isEqualTo("Sin novedad");
        assertThat(guardado.getCategoria()).isEqualTo("CARRO");
    }

    @Test
    void verificarRechazaVehiculoQueNoPerteneceAlUsuarioDelIngreso() {
        Long otroUsuario = crearUsuario("f7-otro-usuario@test.com", Roles.USUARIO,
                parqueaderoId, Roles.USUARIO);
        Long vehiculoAjeno = vehiculoRepository.save(Vehiculo.builder()
                .placa("F7XYZ98")
                .tipo("CARRO")
                .usuarioId(otroUsuario)
                .build()).getId();
        Long ingresoId = crearIngreso("RESERVADO", "F7ABC12");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingresoService.verificar(GUARDIA, ingresoId,
                        VerificarIngresoRequest.builder().vehiculoId(vehiculoAjeno).build()));

        assertThat(ex.getMessage()).contains("no pertenece");
    }

    @Test
    void verificarRechazaPlacaDistintaALaRegistrada() {
        Long ingresoId = crearIngreso("RESERVADO", "F7ABC12");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingresoService.verificar(GUARDIA, ingresoId,
                        VerificarIngresoRequest.builder().placa("OTR1234").build()));

        assertThat(ex.getMessage()).contains("no coincide");
    }

    @Test
    void guardiaDeOtroParqueaderoNoPuedeVerificar() {
        Long ingresoId = crearIngreso("RESERVADO", "F7ABC12");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ingresoService.verificar(OTRO_GUARDIA, ingresoId,
                        VerificarIngresoRequest.builder().novedad("Revisado").build()));

        assertThat(ex.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void verificarRechazaIngresoFinalizado() {
        Long ingresoId = crearIngreso("FINALIZADO", "F7ABC12");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingresoService.verificar(GUARDIA, ingresoId,
                        VerificarIngresoRequest.builder().novedad("Revisado").build()));

        assertThat(ex.getMessage()).contains("reservado o activo");
    }

    @Test
    void empresaYPlataformaPuedenVerificar() {
        Long ingresoId = crearIngreso("ACTIVO", "F7ABC12");

        assertThat(ingresoService.verificar(EMPRESA, ingresoId,
                VerificarIngresoRequest.builder().operador("Empresa F7").build()).getEstado())
                .isEqualTo("ACTIVO");
        assertThat(ingresoService.verificar("f7-plataforma@test.com", ingresoId,
                VerificarIngresoRequest.builder().novedad("Supervisado").build()).getEstado())
                .isEqualTo("ACTIVO");
    }

    @Test
    void estadoPagoSinFacturaIndicaAlDia() {
        Long ingresoId = crearIngreso("ACTIVO", "F7ABC12");

        EstadoPagoResponse estado = estadoPagoService.obtener(GUARDIA, ingresoId);

        assertThat(estado.isAlDia()).isTrue();
        assertThat(estado.getEstado()).isEqualTo("SIN_FACTURA");
        assertThat(estado.getFacturaId()).isNull();
        assertThat(estado.getPagoId()).isNull();
    }

    @Test
    void estadoPagoConFacturaPagadaIndicaAlDia() {
        saldoService.recargar(USUARIO, RecargaRequest.builder().valor(VALOR_HORA).build());
        Long ingresoId = crearIngreso("ACTIVO", "F7ABC12");
        facturaService.generarYCobrar(ingresoRepository.findById(ingresoId).orElseThrow());
        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();

        EstadoPagoResponse estado = estadoPagoService.obtener(CAJERO, ingresoId);

        assertThat(estado.isAlDia()).isTrue();
        assertThat(estado.getEstado()).isEqualTo("AL_DIA");
        assertThat(estado.getFacturaId()).isEqualTo(factura.getId());
        assertThat(estado.getPagoId()).isNotNull();
    }

    @Test
    void estadoPagoConFacturaPendienteIndicaNoAlDia() {
        Long ingresoId = crearIngreso("ACTIVO", "F7ABC12");
        facturaService.generarYCobrar(ingresoRepository.findById(ingresoId).orElseThrow());

        EstadoPagoResponse estado = estadoPagoService.obtener(GUARDIA, ingresoId);

        assertThat(estado.isAlDia()).isFalse();
        assertThat(estado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(estado.getValorFactura()).isEqualByComparingTo(VALOR_HORA);
    }

    @Test
    void estadoPagoConCreditoVencidoIndicaDeudaVencida() {
        Long ingresoId = crearIngreso("ACTIVO", "F7ABC12");
        facturaService.generarYCobrar(ingresoRepository.findById(ingresoId).orElseThrow());
        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElseThrow();
        CreditoResponse credito = creditoService.otorgar(USUARIO,
                CreditoRequest.builder().facturaId(factura.getId()).build());
        Credito vencido = creditoRepository.findById(credito.getId()).orElseThrow();
        vencido.setFechaLimite(LocalDateTime.now().minusDays(1));
        creditoRepository.saveAndFlush(vencido);

        EstadoPagoResponse estado = estadoPagoService.obtener(GUARDIA, ingresoId);

        assertThat(estado.isAlDia()).isFalse();
        assertThat(estado.getEstado()).isEqualTo("DEUDA_VENCIDA");
        assertThat(estado.isCreditoVencido()).isTrue();
        assertThat(estado.getCreditoPendiente()).isEqualByComparingTo(VALOR_HORA);
    }

    private Long crearUsuario(String email, String rol, Long parqueaderoId, String rolAsignado) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(email)
                .rol(rol)
                .nombre(email)
                .build());
        if (parqueaderoId != null) {
            parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                    .parqueaderoId(parqueaderoId)
                    .usuarioId(usuario.getId())
                    .rolAsignado(rolAsignado)
                    .build());
        }
        return usuario.getId();
    }

    private Long crearIngreso(String estado, String placa) {
        Espacio espacio = espacioRepository.save(Espacio.builder()
                .parqueaderoId(parqueaderoId)
                .numero("F7-" + System.nanoTime())
                .tipo("CARRO")
                .estado("LIBRE")
                .build());
        Ingreso ingreso = ingresoRepository.save(Ingreso.builder()
                .folio("F7-" + System.nanoTime())
                .placa(placa)
                .categoria("CARRO")
                .parqueaderoId(parqueaderoId)
                .espacioId(espacio.getId())
                .tarifaId(tarifaId)
                .tarifaValor(VALOR_HORA)
                .estado(estado)
                .usuarioId(usuarioId)
                .build());
        return ingreso.getId();
    }
}
