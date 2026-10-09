package com.cinema.movie_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * movie-service = OAuth2 Resource Server. Valida el JWT RS256 emitido por auth-service
 * usando su JWKS (spring.security.oauth2.resourceserver.jwt.jwk-set-uri).
 *
 * Público : Swagger UI, /actuator/**, /error y la cartelera (GET /movies, GET /screenings).
 * Con JWT : toda escritura (POST/PUT/PATCH/DELETE), incluidas reserve/release del SAGA.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ProblemDetailSecurityHandlers problemHandlers = new ProblemDetailSecurityHandlers();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/movies/**", "/screenings/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(problemHandlers)
                .accessDeniedHandler(problemHandlers)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(problemHandlers)
                .accessDeniedHandler(problemHandlers)
            );

        return http.build();
    }
}
