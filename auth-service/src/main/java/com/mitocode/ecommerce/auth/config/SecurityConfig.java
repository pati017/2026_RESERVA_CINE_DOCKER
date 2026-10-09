package com.mitocode.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del auth-service.
 *
 * Política de acceso:
 *   - /api/v1/auth/**   → público (login, register, refresh)
 *   - /oauth2/**         → público (callback GitHub, JWKS)
 *   - /actuator/health  → público (healthcheck Docker)
 *   - resto              → requiere autenticación (protección por defecto)
 *
 * El auth-service NO es un Resource Server — no valida tokens JWT entrantes.
 * Es el EMISOR de tokens: recibe username/password y devuelve JWT.
 *
 * STATELESS: no se crean sesiones HTTP. Cada request es independiente.
 * CSRF deshabilitado: APIs REST sin estado no necesitan protección CSRF.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     * AuthenticationManager expuesto como Bean.
     * Necesario para que AuthController pueda llamar a authenticate()
     * sin extender WebSecurityConfigurerAdapter (eliminado en Spring Security 6).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
