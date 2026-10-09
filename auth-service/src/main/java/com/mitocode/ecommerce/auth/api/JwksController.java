package com.mitocode.ecommerce.auth.api;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * Endpoint JWKS: expone la clave pública RSA en formato JWK Set.
 * <p>
 * GET /oauth2/jwks
 * Respuesta: { "keys": [{ "kty": "RSA", "e": "AQAB", "n": "...", "kid": "..." }] }
 * <p>
 * Los Resource Servers (api-gateway, order-orchestrator, order-service) llaman
 * a este endpoint al arrancar para descargar la clave pública y validar JWTs
 * localmente sin hacer una llamada HTTP por cada request.
 *
 */
@RestController
public class JwksController {

    private final JWKSet jwkSet;

    public JwksController(JWKSet jwkSet) {
        this.jwkSet = Objects.requireNonNull(jwkSet, "jwkSet must not be null");
    }

    @GetMapping(value = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        // toPublicJWKSet() → solo expone claves públicas (nunca la privada)
        // toJSONObject()   → { "keys": [{ "kty": "RSA", "e": "AQAB", "n": "...", "kid": "..." }] }
        // Este es el formato estándar JWK Set que espera spring-security oauth2ResourceServer
        return jwkSet.toPublicJWKSet().toJSONObject();
    }
}
