package ar.edu.uade.catalogo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.uade.catalogo.dto.CategoriaRequest;
import ar.edu.uade.catalogo.dto.ImagenProductoRequest;
import ar.edu.uade.catalogo.dto.ItemCarritoRequest;
import ar.edu.uade.catalogo.dto.LoginRequest;
import ar.edu.uade.catalogo.dto.MarcaRequest;
import ar.edu.uade.catalogo.dto.ProductoRequest;
import ar.edu.uade.catalogo.dto.RegisterRequest;
import ar.edu.uade.catalogo.model.Rol;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CatalogoApiIntegrationTest {

    private static final String ADMIN_CORREO = "admin@mail.com";
    private static final String ADMIN_CLAVE = "admin123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void flujoCompletoRegistroLoginProductosCarritoYCheckout() throws Exception {
        Long usuarioId = registrarUsuario("comprador", "comprador@mail.com");
        login("comprador@mail.com", "123456")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId))
                .andExpect(jsonPath("$.clave").doesNotExist());

        crearAdmin();

        mvc.perform(get("/api/usuarios").with(httpBasic(ADMIN_CORREO, ADMIN_CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].clave").doesNotExist());

        Long categoriaId = crearCategoria("Snacks");
        Long marcaId = crearMarca("PetShop");
        Long especieId = crearEspecie("Perro");
        Long productoId = crearProducto(usuarioId, categoriaId, marcaId, especieId, "Snack Dental", 3);

        mvc.perform(get("/api/productos")
                .param("soloDisponibles", "true")
                .param("categoriaId", categoriaId.toString())
                .param("marcaId", marcaId.toString())
                .param("especieId", especieId.toString())
                .param("nombre", "dental"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(productoId));

        mvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes", hasSize(1)))
                .andExpect(jsonPath("$.stock").value(3));

        mvc.perform(post("/api/carrito/{usuarioId}/items", usuarioId)
                .with(httpBasic("comprador@mail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new ItemCarritoRequest(productoId, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(251.00));

        MvcResult compra = mvc.perform(post("/api/compras/checkout/{usuarioId}", usuarioId)
                .with(httpBasic("comprador@mail.com", "123456")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId))
                .andExpect(jsonPath("$.total").value(251.00))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$.lineas[0].cantidad").value(2))
                .andReturn();
        Long compraId = id(compra);

        mvc.perform(get("/api/compras/{id}", compraId)
                .with(httpBasic("comprador@mail.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compraId))
                .andExpect(jsonPath("$.lineas[0].productoId").value(productoId));

        mvc.perform(get("/api/compras").param("usuarioId", usuarioId.toString())
                .with(httpBasic("comprador@mail.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(compraId));

        mvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(1));

        mvc.perform(get("/api/carrito/{usuarioId}", usuarioId)
                .with(httpBasic("comprador@mail.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void validaRespuestasHttpDeErroresPrincipales() throws Exception {
        Long usuarioId = registrarUsuario("vendedor", "vendedor@mail.com");
        Long otroUsuarioId = registrarUsuario("otro", "otro@mail.com");
        crearAdmin();
        Long categoriaId = crearCategoria("Alimentos");

        login("vendedor@mail.com", "clave-incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Correo o clave incorrectos"));

        mvc.perform(get("/api/productos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("No existe el producto")));

        mvc.perform(post("/api/productos")
                .with(httpBasic("vendedor@mail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(producto(usuarioId, categoriaId, null, null, "Alimento Premium", 10, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El producto debe tener al menos una imagen"));

        Long productoId = crearProducto(usuarioId, categoriaId, null, null, "Alimento Premium", 1);

        mvc.perform(post("/api/carrito/{usuarioId}/items", usuarioId)
                .with(httpBasic("vendedor@mail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new ItemCarritoRequest(productoId, 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Stock insuficiente")));

        mvc.perform(patch("/api/productos/{id}/stock", productoId)
                .param("usuarioId", otroUsuarioId.toString())
                .with(httpBasic("otro@mail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stock\":5}"))
                .andExpect(status().isForbidden());
    }

    private Long registrarUsuario(String nombreUsuario, String correo) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nombreUsuario(nombreUsuario)
                .correo(correo)
                .clave("123456")
                .nombre("Nombre")
                .apellido("Apellido")
                .build();

        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andReturn();

        return id(result);
    }

    private ResultActionsWrapper login(String correo, String clave) throws Exception {
        LoginRequest request = LoginRequest.builder()
                .correo(correo)
                .clave(clave)
                .build();
        return new ResultActionsWrapper(mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))));
    }

    /**
     * Crea un usuario ADMIN directo por repositorio (no hay endpoint publico
     * para registrarse como admin, y esta bien que no lo haya).
     */
    private void crearAdmin() {
        Usuario admin = Usuario.builder()
                .nombreUsuario("admin")
                .correo(ADMIN_CORREO)
                .clave(passwordEncoder.encode(ADMIN_CLAVE))
                .nombre("Admin")
                .apellido("Sistema")
                .roles(new HashSet<>(Set.of(Rol.ROLE_ADMIN)))
                .build();
        usuarioRepository.save(admin);
    }

    private Long crearCategoria(String nombre) throws Exception {
        MvcResult result = mvc.perform(post("/api/categorias")
                .with(httpBasic(ADMIN_CORREO, ADMIN_CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new CategoriaRequest(nombre, "Descripcion " + nombre, true))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private Long crearMarca(String nombre) throws Exception {
        MvcResult result = mvc.perform(post("/api/marcas")
                .with(httpBasic(ADMIN_CORREO, ADMIN_CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new MarcaRequest(nombre, true))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private Long crearEspecie(String nombre) throws Exception {
        MvcResult result = mvc.perform(post("/api/especies")
                .with(httpBasic(ADMIN_CORREO, ADMIN_CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private Long crearProducto(Long usuarioId, Long categoriaId, Long marcaId, Long especieId, String nombre, int stock)
            throws Exception {
        MvcResult result = mvc.perform(post("/api/productos")
                .with(httpBasic(ADMIN_CORREO, ADMIN_CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(producto(usuarioId, categoriaId, marcaId, especieId, nombre, stock,
                        List.of(new ImagenProductoRequest("/img/" + nombre + ".jpg", 1, true))))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private ProductoRequest producto(Long usuarioId, Long categoriaId, Long marcaId, Long especieId, String nombre,
            int stock, List<ImagenProductoRequest> imagenes) {
        List<Long> especiesIds = especieId == null ? null : List.of(especieId);
        return new ProductoRequest(nombre, "Descripcion de " + nombre, new BigDecimal("125.50"), stock, categoriaId,
                marcaId, usuarioId, true, especiesIds, imagenes);
    }

    private String toJson(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    private Long id(MvcResult result) throws Exception {
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private record ResultActionsWrapper(org.springframework.test.web.servlet.ResultActions actions) {
        ResultActionsWrapper andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            actions.andExpect(matcher);
            return this;
        }
    }
}
