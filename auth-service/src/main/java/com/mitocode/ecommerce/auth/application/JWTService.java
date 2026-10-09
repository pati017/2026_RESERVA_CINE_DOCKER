package com.mitocode.ecommerce.auth.application;

import com.mitocode.ecommerce.auth.api.dto.RegisterRequest;
import com.mitocode.ecommerce.auth.api.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de generación y validación de JWT.
 * <p>
 * Responsabilidades:
 * - Firmar accessToken con clave RSA privada (RS256) vía JwtEncoder
 * - Firmar refreshToken (misma clave, TTL mayor, claim "token_use=refresh")
 * - Validar refreshToken y emitir nuevo par de tokens
 * - Registrar usuarios en InMemoryUserDetailsManager
 * <p>
 * Claims del accessToken:
 * iss  → issuer del servicio (http://localhost:50003)
 * sub  → username
 * iat  → emitido en
 * exp  → expira en (iat + accessTokenTtl)
 * roles → lista de roles Spring Security (["ROLE_ADMIN"])
 * <p>
 * Claims adicionales del refreshToken:
 * token_use → "refresh" (distingue del accessToken)
 * exp        → iat + refreshTokenTtl (más largo)
 */
@Component
public class JWTService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${auth.jwt.issuer}")
    private String issuer;

    @Value("${auth.jwt.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${auth.jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    public JWTService(UserDetailsManager userDetailsManager,
                      PasswordEncoder passwordEncoder,
                      JwtEncoder jwtEncoder,
                      JwtDecoder jwtDecoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Registra un nuevo usuario en el InMemoryUserDetailsManager.
     * El role se normaliza: si no tiene prefijo ROLE_ se añade automáticamente.
     */
    public void register(RegisterRequest request) {
        String role = request.role() != null && !request.role().isBlank() ? request.role() : DEFAULT_ROLE;

        UserDetails newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(role) // User.Builder.roles() añade ROLE_ automáticamente como prefijo del rol
                .build();

        userDetailsManager.createUser(newUser);
    }

    /**
     * Genera un par accessToken + refreshToken para la autenticación dada.
     * Llamado tras autenticación exitosa con username/password.
     */
    public TokenResponse generateTokens(Authentication authentication) {
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = buildAccessToken(authentication.getName(), roles);
        String refreshToken = buildRefreshToken(authentication.getName());

        return TokenResponse.bearer(token, refreshToken, accessTokenTtl); // Refresh token no se genera aquí
    }

    // ── Métodos privados de construcción de tokens ────────────────────────────

    private String buildAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenTtl)) // Access token válido por 5 minutos
                .claim("roles", roles)
                .claim("token_use", "access")// Claim adicional para distinguir el tipo de token
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String buildRefreshToken(String username) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshTokenTtl))
                .claim("token_use", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public TokenResponse refreshTokens(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);

        String tokenUse = jwt.getClaimAsString("token_use");
        if (!"refresh".equals(tokenUse)) {
            throw new JwtException("Invalid token type: expected refresh token");
        }

        String username = jwt.getSubject();

        UserDetails userDetails = userDetailsManager.loadUserByUsername(username); // Verifica que el usuario aún exista
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        String newAccessToken = buildAccessToken(username, roles);

        return TokenResponse.bearer(newAccessToken, refreshToken, accessTokenTtl); // Refresh token no se genera aquí
    }

    /**
     * Genera un par accessToken + refreshToken para un usuario autenticado con GitHub OAuth2.
     * <p>
     * El "sub" del JWT usa el "login" de GitHub (username único, ej: "octocat").
     * El rol asignado es ROLE_USER por defecto — los usuarios externos no son admins.
     * <p>
     * Para conceder ROLE_ADMIN a usuarios específicos de GitHub, comparar
     * oauth2User.getAttribute("login") contra una lista de admins configurada.
     *
     * @param oauth2User perfil del usuario obtenido de la GitHub User API
     */
    public TokenResponse generateTokensFromGitHub(OAuth2User oauth2User) {

        String username = oauth2User.getAttribute("login");
        if (username == null || username.isBlank()) {
            //Fallback: usar el ID numérico de GitHub como sub si no hay login (muy raro)
            Objects id = oauth2User.getAttribute("id");
            username = "github_user_" + id;
        }
        List<String> roles = List.of("ROLE_USER"); // Rol por defecto para usuarios externos

        String token = buildAccessToken(username, roles);
        String refreshToken = buildRefreshToken(username);

        return TokenResponse.bearer(token, refreshToken, accessTokenTtl); // Refresh token no se genera aquí
    }
}
