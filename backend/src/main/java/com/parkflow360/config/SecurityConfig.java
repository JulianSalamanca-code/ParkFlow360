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

                // ---------- Cliente (rol USUARIO) ----------
                // Solicitudes concretas del cliente (van ANTES de las reglas de ADMIN)
                .requestMatchers("/api/vehiculos/mios", "/api/vehiculos/mios/**").hasRole("USUARIO")
                .requestMatchers("/api/ingresos/mios", "/api/ingresos/reservar").hasRole("USUARIO")
                .requestMatchers("/api/pagos/online", "/api/pagos/mios").hasRole("USUARIO")
                // Consulta de catálogo (espacios, tarifas y planos) compartida
                .requestMatchers(HttpMethod.GET, "/api/espacios/**", "/api/tarifas/**", "/api/planos/**")
                    .hasAnyRole("ADMIN", "USUARIO")

                // ---------- Solo ADMIN ----------
                // El administrador NO registra vehículos ni reserva: solo opera y supervisa.
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/reportes/**").hasRole("ADMIN")
                .requestMatchers("/api/planos/**").hasRole("ADMIN")
                .requestMatchers("/api/pagos/**").hasRole("ADMIN")
                .requestMatchers("/api/vehiculos/**").hasRole("ADMIN")
                .requestMatchers("/api/ingresos/**").hasRole("ADMIN")
                .requestMatchers("/api/espacios/**").hasRole("ADMIN")
                .requestMatchers("/api/tarifas/**").hasRole("ADMIN")

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
