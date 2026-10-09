package com.mitocode.ecommerce.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales para autenticación username/password.
 * Usada en POST /api/v1/auth/login (perfil jwt).
 */
public record LoginRequest(

        @NotBlank(message = "username es obligatorio")
        String username,

        @NotBlank(message = "password es obligatorio")
        String password
) {
}
