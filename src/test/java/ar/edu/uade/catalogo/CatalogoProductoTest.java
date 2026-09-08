package ar.edu.uade.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.uade.catalogo.controller.ProductoController;
import ar.edu.uade.catalogo.dto.ImagenProductoRequest;
import ar.edu.uade.catalogo.dto.ProductoRequest;
import ar.edu.uade.catalogo.exception.GlobalExceptionHandler;
import ar.edu.uade.catalogo.model.Categoria;
import ar.edu.uade.catalogo.model.Producto;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.CategoriaRepository;
import ar.edu.uade.catalogo.repository.EspecieRepository;
import ar.edu.uade.catalogo.repository.MarcaRepository;
import ar.edu.uade.catalogo.repository.ProductoRepository;
import ar.edu.uade.catalogo.repository.UsuarioRepository;
import ar.edu.uade.catalogo.service.ProductoService;

import static org.mockito.Mockito.mock;

// Controller y service reales; persistencia simulada para ejecutar sin MySQL.
class CatalogoProductoTest {
    private ProductoRepository productos;
    private Producto producto;
    private MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void preparar() {
        productos = mock(ProductoRepository.class);
        CategoriaRepository categorias = mock(CategoriaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        ProductoService service = new ProductoService(productos, categorias,
                mock(MarcaRepository.class), mock(EspecieRepository.class), usuarios);
        mvc = MockMvcBuilders.standaloneSetup(new ProductoController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Alimentos");
        Usuario creador = new Usuario();
        creador.setId(1L);
        creador.setClave("no-debe-aparecer-en-el-catalogo");
        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Alimento");
        producto.setDescripcion("Alimento para mascotas");
        producto.setPrecio(BigDecimal.TEN);
        producto.setCategoria(categoria);
        producto.setCreador(creador);
        when(productos.findById(10L)).thenReturn(Optional.of(producto));
        when(productos.save(any(Producto.class))).thenAnswer(invocacion -> {
            Producto guardado = invocacion.getArgument(0);
            guardado.setId(10L);
            producto = guardado;
            return guardado;
        });
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(usuarios.findById(1L)).thenReturn(Optional.of(creador));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n\t"})
    void rechazaPublicarOActualizarSinDescripcion(String descripcion) throws Exception {
        String body = json.writeValueAsString(request(descripcion, imagenes()));
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/productos/10").param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        assertThat(producto.getDescripcion()).isEqualTo("Alimento para mascotas");
    }

    @Test
    void publicaVariasImagenesYDevuelveDetalleOrdenadoSinClaveDelCreador() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion completa", imagenes()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagenes.length()").value(2))
                .andExpect(jsonPath("$.imagenes[0].ubicacion").value("/imagenes/frente.jpg"))
                .andExpect(jsonPath("$.imagenes[0].principal").value(true));
        when(productos.findById(10L)).thenReturn(Optional.of(producto));
        mvc.perform(get("/api/productos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Descripcion completa"))
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.imagenes[1].posicion").value(2))
                .andExpect(jsonPath("$.creador.clave").doesNotExist());
        assertThat(producto.getImagenes()).allSatisfy(imagen ->
                assertThat(imagen.getProducto()).isSameAs(producto));
    }

    @Test
    void actualizaConUnaImagenYLaMarcaPrincipalPorDefecto() throws Exception {
        mvc.perform(put("/api/productos/10").param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Nueva descripcion",
                        List.of(new ImagenProductoRequest("/imagenes/unica.jpg", null, null))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes.length()").value(1))
                .andExpect(jsonPath("$.imagenes[0].principal").value(true))
                .andExpect(jsonPath("$.imagenes[0].posicion").value(1));
    }

    @Test
    void rechazaAltaSinImagenesYActualizacionConListaVacia() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion", null))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/productos/10").param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion", List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaImagenSinUbicacion() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion",
                        List.of(new ImagenProductoRequest("  ", null, null))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void conservaImagenesCuandoSeOmiteLaListaEnActualizacion() throws Exception {
        mvc.perform(put("/api/productos/10").param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion", imagenes()))))
                .andExpect(status().isOk());
        mvc.perform(put("/api/productos/10").param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Otra descripcion", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes.length()").value(2));
    }

    @Test
    void respetaFiltrosYNormalizaElNombreAntesDeConsultar() throws Exception {
        when(productos.buscar(true, 1L, 2L, 3L, 1L, "ALI"))
                .thenReturn(List.of(producto));
        mvc.perform(get("/api/productos").param("soloDisponibles", "true")
                .param("categoriaId", "1").param("marcaId", "2").param("especieId", "3")
                .param("creadorId", "1").param("nombre", "  ALI  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void devuelve404ParaDetalleInexistenteY403ParaEdicionAjena() throws Exception {
        mvc.perform(get("/api/productos/999")).andExpect(status().isNotFound());
        mvc.perform(put("/api/productos/10").param("usuarioId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request("Descripcion", imagenes()))))
                .andExpect(status().isForbidden());
    }

    private ProductoRequest request(String descripcion, List<ImagenProductoRequest> imagenes) {
        return new ProductoRequest("Alimento", descripcion, BigDecimal.TEN, 0, 1L,
                null, 1L, true, null, imagenes);
    }

    private List<ImagenProductoRequest> imagenes() {
        return List.of(new ImagenProductoRequest("/imagenes/dorso.jpg", 2, false),
                new ImagenProductoRequest("/imagenes/frente.jpg", 1, true));
    }
}
