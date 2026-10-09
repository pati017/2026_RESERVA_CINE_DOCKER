package com.mitocode.ecommerce.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta de autenticación exitosa.
 * Devuelve accessToken JWT (RS256), refreshToken y tiempo de expiración en segundos.
 */
public record TokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        // Tiempo de vida del accessToken en segundos (ej: 3600 = 1 hora)
        @JsonProperty("expires_in")
        long expiresIn,

        // Tipo de token — siempre "Bearer" para JWT
        @JsonProperty("token_type")
        String tokenType
) {
    // Factory method para crear respuesta estándar Bearer
    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer");
    }
}
