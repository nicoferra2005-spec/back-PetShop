package ar.edu.uade.catalogo.config;

import ar.edu.uade.catalogo.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * BCrypt es el algoritmo recomendado por Spring Security: genera su propia
     * salt por contraseña y el costo es ajustable.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Se instancia acá en vez de exponerlo como {@code @Bean} porque un
     * {@code AuthenticationProvider} publicado en el contexto desactiva el
     * cableado automático del {@code UserDetailsService}.
     */
    private DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                             PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UsuarioDetailsService usuarioDetailsService,
                                                   PasswordEncoder passwordEncoder) throws Exception {
        return http
                .authenticationProvider(authenticationProvider(usuarioDetailsService, passwordEncoder))
                // La API no usa formularios ni cookies de sesión, así que el token
                // CSRF no aporta nada y rompería a los clientes REST.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> {
                })
                // Todos los endpoints quedan abiertos a propósito: esta entrega cubre
                // la base de seguridad (usuario, roles, UserDetails y PasswordEncoder),
                // no la autorización por endpoint. Restringir acá sin un mecanismo de
                // login end-to-end dejaría la API y el front sin poder operar.
                // Las reglas por rol van en este mismo bloque cuando se agreguen, por
                // ejemplo:
                //   .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios/registro", "/api/usuarios/login").permitAll()
                        .anyRequest().permitAll())
                .build();
    }
}
