package com.mitocode.ecommerce.auth.api;

import com.nimbusds.jose.jwk.JWKSet;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Endpoint de inicio del flujo GitHub OAuth2.
 *
 * GET /api/v1/auth/login/oauth2/github
 *   → 302 redirect a /oauth2/authorization/github
 *   → Spring OAuth2 redirige a GitHub con los parámetros necesarios
 *
 * Este endpoint existe como "alias" amigable para clientes de la API
 * que conocen la ruta /api/v1/auth/** pero no la URL interna de Spring OAuth2.
 *
 * IMPORTANTE: Este RestController es un inner bean de GitHubOAuth2Config.
 * Solo existe cuando el perfil "github" está activo — igual que esta config.
 * El request llega a SecurityConfig.securityFilterChain (que tiene /api/v1/auth/** permitAll).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class GithubRedirectController {

    @GetMapping(value = "/login/oauth2/github")
    public void initiateGitHubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }

}
