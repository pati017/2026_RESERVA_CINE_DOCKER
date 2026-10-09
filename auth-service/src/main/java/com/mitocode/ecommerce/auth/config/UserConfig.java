package com.mitocode.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Usuarios en memoria para el perfil 'jwt'.
 *
 * En producción reemplazar por:
 *   - UserDetailsService que consulte base de datos (JPA + tabla users)
 *   - O delegar a un proveedor externo (LDAP, OAuth2)
 *
 * Usuarios disponibles:
 *   admin / admin123 → ROLE_ADMIN
 *   user  / user123  → ROLE_USER
 *
 * Nota: InMemoryUserDetailsManager implementa UserDetailsService Y UserDetailsManager
 * (que añade createUser/updateUser/deleteUser). Se expone como ambos tipos para que:
 *   - Spring Security use UserDetailsService para autenticar
 *   - JwtTokenService use InMemoryUserDetailsManager para registrar nuevos usuarios
 */
@Configuration
public class UserConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 10 (default) — seguro para producción
        return new BCryptPasswordEncoder();
    }

    /*
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin";
        System.out.println(encoder.encode(rawPassword));
        System.out.println(encoder.encode(rawPassword));
        System.out.println(encoder.encode(rawPassword));
        System.out.println(encoder.encode(rawPassword));
        System.out.println(encoder.encode(rawPassword));

        String passEnBaseDeDatos = encoder.encode(rawPassword);

        System.out.println(encoder.matches(rawPassword, passEnBaseDeDatos));
    }
    */

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder){
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")  // Spring añade automáticamente el prefijo ROLE_
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

}
