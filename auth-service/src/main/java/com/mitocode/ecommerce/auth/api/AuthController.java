package com.mitocode.ecommerce.auth.api;

import com.mitocode.ecommerce.auth.api.dto.LoginRequest;
import com.mitocode.ecommerce.auth.api.dto.RefreshRequest;
import com.mitocode.ecommerce.auth.api.dto.RegisterRequest;
import com.mitocode.ecommerce.auth.api.dto.TokenResponse;
import com.mitocode.ecommerce.auth.application.JWTService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP para autenticación.
 * <p>
 * Endpoints:
 * POST /api/v1/auth/register  → registra usuario (placeholder en memoria)
 * POST /api/v1/auth/login     → autentica y devuelve JWT RS256
 * POST /api/v1/auth/refresh   → renueva accessToken con refreshToken
 * <p>
 * No expone lógica de dominio: delega completamente a JwtTokenService.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registra un nuevo usuario en memoria.
     * En un sistema real persistiría en base de datos.
     * Responde 200 OK con mensaje de confirmación.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        jwtService.register(request);
        return ResponseEntity.ok("Usuario registrado: " + request.username());
    }

    /**
     * Autentica con username/password y emite un JWT RS256.
     * Spring Security valida las credenciales contra InMemoryUserDetailsManager.
     * El JWT lleva claims: sub, roles, iss, iat, exp.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        // Delegar autenticación a Spring Security — lanza AuthenticationException si falla
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        TokenResponse tokenResponse = jwtService.generateTokens(authentication);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Renueva el accessToken usando el refreshToken.
     * Valida el refreshToken con el JwtDecoder y emite un nuevo par de tokens.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse tokenResponse = jwtService.refreshTokens(request.refreshToken());
        return ResponseEntity.ok(tokenResponse);
    }
}
