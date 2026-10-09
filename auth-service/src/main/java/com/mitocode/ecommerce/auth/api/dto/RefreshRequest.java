package com.mitocode.ecommerce.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Token de refresco para obtener un nuevo accessToken sin re-autenticar.
 * POST /api/v1/auth/refresh
 */
public record RefreshRequest(

        @NotBlank(message = "refreshToken es obligatorio")
        String refreshToken
) {
}
