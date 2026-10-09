package com.cinema.booking_service.infrastructure.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

/**
 * Token relay: reenvía a movie-service el MISMO JWT con el que el usuario llamó a booking-service.
 * Así movie-service autentica al usuario real (no a un "servicio") y no hace falta otro login.
 *
 * Funciona porque el SAGA se ejecuta de forma síncrona en el hilo de la petición HTTP, donde
 * el SecurityContext ya contiene el JwtAuthenticationToken.
 */
public class BearerTokenRelayInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
        }
        return execution.execute(request, body);
    }
}
