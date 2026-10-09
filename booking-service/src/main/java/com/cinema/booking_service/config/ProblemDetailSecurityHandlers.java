package com.cinema.booking_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Los errores 401/403 los genera el filtro de seguridad (antes de llegar a un controller),
 * por eso @RestControllerAdvice no los ve. Aquí se emiten también como RFC 7807 (ProblemDetail).
 */
public class ProblemDetailSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(request, response, 401, "Unauthorized", "Token JWT ausente, inválido o expirado");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(request, response, 403, "Forbidden", "No tienes permisos para este recurso");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
                       int status, String title, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        String instance = request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"" + title + "\",\"status\":" + status
                        + ",\"detail\":\"" + detail + "\",\"instance\":\"" + instance + "\"}");
    }
}
