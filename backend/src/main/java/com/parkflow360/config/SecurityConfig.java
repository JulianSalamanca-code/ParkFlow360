package com.parkflow360.config;

import com.parkflow360.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---------- Público ----------
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pagos/wompi/webhook").permitAll()

                // ---------- Cliente (USUARIO) ----------
                .requestMatchers("/api/vehiculos/mios", "/api/vehiculos/mios/**").hasRole("USUARIO")
                .requestMatchers("/api/ingresos/mios", "/api/ingresos/reservar").hasRole("USUARIO")
                .requestMatchers("/api/pagos/online", "/api/pagos/mios").hasRole("USUARIO")
                .requestMatchers("/api/saldos/**").hasAnyRole("USUARIO", "PLATAFORMA")
                .requestMatchers("/api/facturas/**").hasAnyRole("USUARIO", "CAJERO_PAGOS", "PLATAFORMA")
                .requestMatchers(HttpMethod.POST, "/api/creditos").hasRole("USUARIO")
                .requestMatchers("/api/creditos/**").hasAnyRole("USUARIO", "CAJERO_PAGOS", "PLATAFORMA")

                // ---------- Vinculación y catálogo compartido ----------
                .requestMatchers("/api/parqueaderos/vincular", "/api/parqueaderos/mios")
                    .hasAnyRole("USUARIO", "EMPRESA", "PLATAFORMA", "CAJERO_PAGOS", "GUARDIA")
                .requestMatchers(HttpMethod.GET, "/api/parqueaderos/*/tipos")
                    .hasAnyRole("USUARIO", "EMPRESA", "PLATAFORMA", "CAJERO_PAGOS", "GUARDIA")
                .requestMatchers(HttpMethod.GET, "/api/espacios/**", "/api/tarifas/**", "/api/planos/**")
                    .hasAnyRole("USUARIO", "EMPRESA", "PLATAFORMA", "CAJERO_PAGOS", "GUARDIA")

                // ---------- Guardia: campo, sin gestión de pagos ----------
                .requestMatchers("/api/ingresos/*/llegada", "/api/ingresos/*/verificar",
                        "/api/ingresos/*/estado-pago")
                    .hasAnyRole("GUARDIA", "CAJERO_PAGOS", "PLATAFORMA")

                // ---------- Cajero: solo pagos ----------
                .requestMatchers("/api/pagos/**")
                    .hasAnyRole("CAJERO_PAGOS", "PLATAFORMA")

                // ---------- Empresa: sus parqueaderos ----------
                .requestMatchers("/api/empresas/**", "/api/parqueaderos/**")
                    .hasAnyRole("EMPRESA", "PLATAFORMA")

                // ---------- Plataforma: administración y supervisión ----------
                .requestMatchers("/api/usuarios/**", "/api/reportes/**", "/api/ingresos/**",
                        "/api/espacios/**", "/api/tarifas/**", "/api/planos/**",
                        "/api/vehiculos/**")
                    .hasRole("PLATAFORMA")

                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
