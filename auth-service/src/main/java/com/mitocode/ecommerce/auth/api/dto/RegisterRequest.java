package com.mitocode.ecommerce.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar un nuevo usuario.
 * Placeholder educativo — en producción persistir en base de datos.
 * POST /api/v1/auth/register
 */
public record RegisterRequest(

        @NotBlank(message = "username es obligatorio")
        String username,

        @NotBlank(message = "password es obligatorio")
        @Size(min = 6, message = "password debe tener al menos 6 caracteres")
        String password,

        // Rol asignado al usuario (ej: ROLE_USER, ROLE_ADMIN)
        // Si no se especifica se asigna ROLE_USER por defecto
        String role
) {
}
