package ar.edu.uade.catalogo.config;

import ar.edu.uade.catalogo.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                // Reglas por rol: catalogo de lectura publico, gestion del catalogo
                // (categorias/marcas/especies) solo ADMIN, listado completo de
                // usuarios solo ADMIN, y todo lo demas requiere estar logueado
                // (cliente o admin). El chequeo de "solo el creador puede editar
                // su propio producto" es un chequeo de negocio aparte, resuelto en
                // ProductoService.validarCreador, no acá.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/marcas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/especies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/marcas/**", "/api/especies/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/marcas/**", "/api/especies/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/marcas/**", "/api/especies/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .build();
    }
}
