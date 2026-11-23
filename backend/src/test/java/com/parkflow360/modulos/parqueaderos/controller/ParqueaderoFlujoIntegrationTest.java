package com.parkflow360.modulos.parqueaderos.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.security.JwtService;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParqueaderoFlujoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaUsuarioRepository empresaUsuarioRepository;
    @Autowired
    private EspacioRepository espacioRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;

    @Test
    void crearParqueaderoYListarloEnElMismoFlujoHttp() throws Exception {
        Usuario empresaUsuario = usuarioRepository.save(Usuario.builder()
                .email("flujo-empresa@test.com")
                .password("hash")
                .rol(Roles.EMPRESA)
                .nombre("Empresa flujo")
                .build());
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa flujo")
                .identificador("EMP-FLUJO")
                .build());
        empresaUsuarioRepository.save(EmpresaUsuario.builder()
                .empresaId(empresa.getId())
                .usuarioId(empresaUsuario.getId())
                .build());
        String token = jwtService.generateToken(
                empresaUsuario.getEmail(), Roles.EMPRESA, empresaUsuario.getNombre());

        Map<String, Object> request = Map.of(
                "nombre", "Sede recién creada",
                "identificador", "FLUJO-01",
                "capacidadVehiculos", 10,
                "tipos", List.of(Map.of("tipo", "CARRO", "cantidadFija", true, "cantidad", 2)),
                "espacios", List.of(
                        Map.of("numero", "CARRO-001", "tipo", "CARRO", "piso", 1),
                        Map.of("numero", "CARRO-002", "tipo", "CARRO", "piso", 1)),
                "cajeroEmail", "cajero-flujo@test.com",
                "cajeroPassword", "clave1234",
                "cajeroNombre", "Cajero flujo");

        MvcResult creado = mockMvc.perform(post("/api/empresas/{id}/parqueaderos", empresa.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificador").value("FLUJO-01"))
                .andReturn();

        JsonNode body = objectMapper.readTree(creado.getResponse().getContentAsString());
        long parqueaderoId = body.get("id").asLong();
        assertThat(espacioRepository.findByParqueaderoId(parqueaderoId)).hasSize(2);

        mockMvc.perform(get("/api/parqueaderos/mios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + parqueaderoId + ")]").exists())
                .andExpect(jsonPath("$[?(@.identificador == 'FLUJO-01')]").exists());
    }

    @Test
    void empresaSinVinculoCreaSuEmpresaYDespuesUnParqueadero() throws Exception {
        Usuario empresaUsuario = usuarioRepository.save(Usuario.builder()
                .email("empresa-onboarding@test.com")
                .password("hash")
                .rol(Roles.EMPRESA)
                .nombre("Empresa onboarding")
                .build());
        String token = jwtService.generateToken(
                empresaUsuario.getEmail(), Roles.EMPRESA, empresaUsuario.getNombre());

        MvcResult empresaCreada = mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Empresa onboarding",
                                "identificador", "EMP-ONBOARD",
                                // Un EMPRESA no puede elegir otro propietario: se ignora.
                                "usuarioEmail", "no-existe@test.com",
                                "toleranciaSalidaMinutos", 30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificador").value("EMP-ONBOARD"))
                .andReturn();
        long empresaId = objectMapper.readTree(empresaCreada.getResponse().getContentAsString())
                .get("id").asLong();

        assertThat(empresaUsuarioRepository.existsByEmpresaIdAndUsuarioId(
                empresaId, empresaUsuario.getId())).isTrue();

        mockMvc.perform(get("/api/empresas/mias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(empresaId));

        mockMvc.perform(post("/api/empresas/{id}/parqueaderos", empresaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Sede onboarding",
                                "identificador", "ONBOARD-01",
                                "cajeroEmail", "cajero-onboarding@test.com",
                                "cajeroPassword", "clave1234"))))
                .andExpect(status().isCreated());
    }

    @Test
    void plataformaSinVinculacionesVeTodosLosParqueaderos() throws Exception {
        Usuario plataforma = usuarioRepository.save(Usuario.builder()
                .email("flujo-plataforma@test.com")
                .password("hash")
                .rol(Roles.PLATAFORMA)
                .nombre("Plataforma flujo")
                .build());
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nombre("Empresa sin vínculo")
                .identificador("EMP-SIN-VINCULO")
                .build());
        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre("Sede plataforma")
                .identificador("PLAT-01")
                .activo(true)
                .build());
        String token = jwtService.generateToken(
                plataforma.getEmail(), Roles.PLATAFORMA, plataforma.getNombre());

        mockMvc.perform(get("/api/parqueaderos/mios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + parqueadero.getId() + ")]").exists());
    }
}
