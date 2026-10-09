package com.mitocode.ecommerce.auth.config.github;

import com.mitocode.ecommerce.auth.api.dto.TokenResponse;
import com.mitocode.ecommerce.auth.application.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.ObjectMapper;


/**
 * Configura el login con GitHub vía OAuth2 Authorization Code.
 * <p>
 * Solo activo con: -Dspring.profiles.active=github
 * <p>
 * ── Flujo completo ────────────────────────────────────────────────────────────
 * <p>
 * 1. Cliente llama:  GET /api/v1/auth/login/oauth2/github
 * 2. auth-service → 302 redirect a /oauth2/authorization/github
 * 3. Spring OAuth2 → 302 redirect a https://github.com/login/oauth/authorize
 * con: client_id, scope, state (CSRF), redirect_uri
 * 4. Usuario autoriza la app en GitHub
 * 5. GitHub → 302 redirect a http://localhost:50003/login/oauth2/code/github
 * con: code, state
 * 6. Spring intercambia code → GitHub access_token
 * 7. Spring consulta api.github.com/user con el access_token → OAuth2User
 * 8. GitHubSuccessHandler genera nuestro JWT (RS256) con datos del usuario
 * 9. Responde con TokenResponse JSON (mismo formato que POST /auth/login)
 * <p>
 * ── Por qué dos SecurityFilterChain ──────────────────────────────────────────
 * <p>
 * SecurityConfig (default order) maneja todos los requests con SessionCreationPolicy.STATELESS.
 * El flujo OAuth2 necesita sesión para el parámetro "state" (protección CSRF entre
 * el step 3 y el step 5 — GitHub verifica que el state enviado == state recibido).
 *
 * @Order(1) + securityMatcher("/oauth2/**", "/login/**") → este filter chain
 * intercepta los paths del flujo OAuth2 ANTES que SecurityConfig.
 * Para todos los demás paths (/api/v1/auth/**), SecurityConfig sigue siendo STATELESS.
 * <p>
 * ── Registro en GitHub OAuth App ─────────────────────────────────────────────
 * <p>
 * https://github.com/settings/developers → OAuth Apps → New OAuth App
 * Application name:        Spring Boot Cloud (dev)
 * Homepage URL:            http://localhost:50003
 * Authorization callback URL: http://localhost:50003/login/oauth2/code/github
 * <p>
 * ── Variables de entorno requeridas ──────────────────────────────────────────
 * <p>
 * GITHUB_CLIENT_ID=<tu-client-id>
 * GITHUB_CLIENT_SECRET=<tu-client-secret>
 * O configurar en application-github.yml
 */
@Configuration
@Profile("github")
public class GitHubOAuth2Config {

    private final JWTService jwtService;

    public GitHubOAuth2Config(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Filter chain para el flujo OAuth2 Authorization Code (GitHub).
     *
     * securityMatcher("/oauth2/**", "/login/**") → este filter chain solo aplica a:
     *   GET /oauth2/authorization/github  → Spring genera URL de autorización GitHub
     *   GET /login/oauth2/code/github     → Spring recibe callback de GitHub
     *
     * Los demás requests (/api/v1/auth/**, /actuator/**) caen al SecurityConfig.
     *
     * SIN sessionManagement(STATELESS) → Spring puede usar sesión para el state OAuth2.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/**")
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(gitHubSuccessHandler())
                );

        return http.build();
    }


    /**
     * Handler invocado tras autenticación OAuth2 exitosa con GitHub.
     *
     * Atributos de OAuth2User (GitHub API — api.github.com/user):
     *   "login"  → username en GitHub (ej: "octocat") — SIEMPRE presente
     *   "name"   → nombre completo (puede ser null si no lo configuró)
     *   "email"  → email (null si el usuario no lo hace público en su perfil)
     *   "id"     → ID numérico único del usuario en GitHub
     *   "avatar_url" → URL del avatar
     *
     * Escribe el TokenResponse como JSON en la respuesta HTTP (sin redirect).
     * El cliente recibe el mismo formato TokenResponse que con POST /auth/login.
     */
    @Bean
    public AuthenticationSuccessHandler gitHubSuccessHandler() {
        ObjectMapper mapper = new ObjectMapper();

        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            TokenResponse tokenResponse = jwtService.generateTokensFromGitHub(oauth2User);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getOutputStream(), tokenResponse);
        };
    }
}
