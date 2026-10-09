package com.cinema.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * booking-service = OAuth2 Resource Server (JWT RS256 de auth-service, validado con su JWKS).
 *
 * Público : Swagger UI, /actuator/**, /error.
 * Con JWT : TODO lo demás. Las reservas son datos personales, por eso también el GET exige token
 * (y además solo su dueño o un ADMIN puede leerlas — ver BookingService#getById).
 *
 * El mismo JWT se reenvía a movie-service (BearerTokenRelayInterceptor).
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
