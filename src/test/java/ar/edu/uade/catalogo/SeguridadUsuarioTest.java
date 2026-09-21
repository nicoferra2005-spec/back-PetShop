package ar.edu.uade.catalogo;

import ar.edu.uade.catalogo.dto.LoginRequest;
import ar.edu.uade.catalogo.dto.RegisterRequest;
import ar.edu.uade.catalogo.model.Rol;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.UsuarioRepository;
import ar.edu.uade.catalogo.security.UsuarioDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeguridadUsuarioTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void registraLaClaveEncriptadaYAsignaRolCliente() throws Exception {
        registrar("ana", "ana@mail.com", "clave-secreta")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CLIENTE"));

        Usuario guardado = usuarioRepository.findByCorreo("ana@mail.com").orElseThrow();

        assertThat(guardado.getClave()).isNotEqualTo("clave-secreta");
        assertThat(guardado.getClave()).startsWith("$2");
        assertThat(passwordEncoder.matches("clave-secreta", guardado.getClave())).isTrue();
        assertThat(guardado.getRoles()).containsExactly(Rol.ROLE_CLIENTE);
    }

    @Test
    void permiteLoguearseConLaClaveOriginal() throws Exception {
        registrar("bruno", "bruno@mail.com", "clave-secreta").andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        LoginRequest.builder().correo("bruno@mail.com").clave("clave-secreta").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("bruno@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CLIENTE"));
    }

    @Test
    void rechazaElLoginConLaClaveIncorrecta() throws Exception {
        registrar("carla", "carla@mail.com", "clave-secreta").andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        LoginRequest.builder().correo("carla@mail.com").clave("otra-clave").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o clave incorrectos"));
    }

    @Test
    void devuelveBadRequestConElDetalleDeLosCamposInvalidos() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(RegisterRequest.builder()
                        .nombreUsuario("")
                        .correo("no-es-un-correo")
                        .clave("123")
                        .nombre("Nombre")
                        .apellido("Apellido")
                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hay campos inválidos en la solicitud."))
                .andExpect(jsonPath("$.validationErrors.nombreUsuario").exists())
                .andExpect(jsonPath("$.validationErrors.correo").exists())
                .andExpect(jsonPath("$.validationErrors.clave").exists());
    }

    @Test
    void elUserDetailsServiceExponeElCorreoYLasAutoridades() throws Exception {
        registrar("diego", "diego@mail.com", "clave-secreta").andExpect(status().isCreated());

        UserDetails detalles = usuarioDetailsService.loadUserByUsername("diego@mail.com");

        assertThat(detalles.getUsername()).isEqualTo("diego@mail.com");
        assertThat(detalles.isEnabled()).isTrue();
        assertThat(detalles.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CLIENTE");

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("nadie@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private org.springframework.test.web.servlet.ResultActions registrar(String nombreUsuario,
                                                                         String correo,
                                                                         String clave) throws Exception {
        return mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(RegisterRequest.builder()
                        .nombreUsuario(nombreUsuario)
                        .correo(correo)
                        .clave(clave)
                        .nombre("Nombre")
                        .apellido("Apellido")
                        .build())));
    }
}
