package ar.edu.uade.catalogo.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.ActualizarStockRequest;
import ar.edu.uade.catalogo.dto.CategoriaResponse;
import ar.edu.uade.catalogo.dto.CreadorResponse;
import ar.edu.uade.catalogo.dto.EspecieResponse;
import ar.edu.uade.catalogo.dto.ImagenProductoRequest;
import ar.edu.uade.catalogo.dto.ImagenProductoResponse;
import ar.edu.uade.catalogo.dto.MarcaResponse;
import ar.edu.uade.catalogo.dto.ProductoRequest;
import ar.edu.uade.catalogo.dto.ProductoResponse;
import ar.edu.uade.catalogo.exception.OperacionNoPermitidaException;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Categoria;
import ar.edu.uade.catalogo.model.Especie;
import ar.edu.uade.catalogo.model.ImagenProducto;
import ar.edu.uade.catalogo.model.Marca;
import ar.edu.uade.catalogo.model.Producto;
import ar.edu.uade.catalogo.model.ProductoEspecie;
import ar.edu.uade.catalogo.model.ProductoEspecieId;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.CategoriaRepository;
import ar.edu.uade.catalogo.repository.EspecieRepository;
import ar.edu.uade.catalogo.repository.MarcaRepository;
import ar.edu.uade.catalogo.repository.ProductoRepository;
import ar.edu.uade.catalogo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final EspecieRepository especieRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository, EspecieRepository especieRepository,
            UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.especieRepository = especieRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProductoResponse> listar(boolean soloDisponibles, Long categoriaId, Long marcaId, Long especieId,
            Long creadorId, String nombre) {
        String filtroNombre = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        return productoRepository.buscar(soloDisponibles, categoriaId, marcaId, especieId, creadorId, filtroNombre)
                .stream().map(this::toResponse).toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        return toResponse(obtener(id));
    }

    public ProductoResponse crear(ProductoRequest request) {
        validarDatos(request);
        if (request.creadorId() == null) {
            throw new IllegalArgumentException("El usuario creador del producto es obligatorio");
        }
        validarImagenes(request.imagenes(), true);

        Producto producto = new Producto();
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(obtenerCategoria(request.categoriaId()));
        producto.setMarca(obtenerMarca(request.marcaId()));
        producto.setCreador(obtenerUsuario(request.creadorId()));
        producto.setDisponible(request.disponible() == null || request.disponible());

        Producto guardado = productoRepository.save(producto);
        sincronizarEspecies(guardado, request.especiesIds());
        reemplazarImagenes(guardado, request.imagenes());
        return toResponse(productoRepository.save(guardado));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request, Long usuarioId) {
        Producto producto = obtener(id);
        validarCreador(producto, usuarioId);
        validarDatos(request);
        validarImagenes(request.imagenes(), false);

        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(obtenerCategoria(request.categoriaId()));
        producto.setMarca(obtenerMarca(request.marcaId()));
        if (request.disponible() != null) {
            producto.setDisponible(request.disponible());
        }
        sincronizarEspecies(producto, request.especiesIds());
        reemplazarImagenes(producto, request.imagenes());
        return toResponse(productoRepository.save(producto));
    }

    public ProductoResponse actualizarStock(Long id, ActualizarStockRequest request, Long usuarioId) {
        Producto producto = obtener(id);
        validarCreador(producto, usuarioId);
        if (request == null || (request.stock() == null && request.ajuste() == null)) {
            throw new IllegalArgumentException("Indica el stock nuevo o el ajuste a aplicar");
        }
        if (request.stock() != null && request.ajuste() != null) {
            throw new IllegalArgumentException("Envia el stock nuevo o el ajuste, pero no los dos juntos");
        }
        int nuevoStock = request.stock() != null ? request.stock() : producto.getStock() + request.ajuste();
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede quedar negativo");
        }
        producto.setStock(nuevoStock);
        return toResponse(productoRepository.save(producto));
    }

    // Baja logica: el producto queda no disponible para conservar el historial de
    // carritos y compras que lo referencian.
    public void eliminar(Long id, Long usuarioId) {
        Producto producto = obtener(id);
        validarCreador(producto, usuarioId);
        producto.setDisponible(false);
        productoRepository.save(producto);
    }

    private Producto obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del producto es obligatorio");
        }
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id: " + id));
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria con id: " + categoriaId));
    }

    private Marca obtenerMarca(Long marcaId) {
        if (marcaId == null) {
            return null;
        }
        return marcaRepository.findById(marcaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la marca con id: " + marcaId));
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id: " + usuarioId));
    }

    private void validarDatos(ProductoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del producto es obligatoria");
        }
        if (request.precio() == null || request.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor o igual a 0");
        }
        if (request.stock() == null || request.stock() < 0) {
            throw new IllegalArgumentException("El stock del producto debe ser mayor o igual a 0");
        }
        if (request.categoriaId() == null) {
            throw new IllegalArgumentException("La categoria del producto es obligatoria");
        }
    }

    private void validarImagenes(List<ImagenProductoRequest> imagenes, boolean obligatorias) {
        if (imagenes == null) {
            if (obligatorias) {
                throw new IllegalArgumentException("El producto debe tener al menos una imagen");
            }
            return;
        }
        if (imagenes.isEmpty()) {
            throw new IllegalArgumentException("El producto debe tener al menos una imagen");
        }
        for (ImagenProductoRequest imagen : imagenes) {
            if (imagen == null || imagen.ubicacion() == null || imagen.ubicacion().isBlank()) {
                throw new IllegalArgumentException("La ubicacion de la imagen es obligatoria");
            }
        }
    }

    private void validarCreador(Producto producto, Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El id del usuario es obligatorio para gestionar el producto");
        }
        if (producto.getCreador() == null || !producto.getCreador().getId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("Solo el usuario que publico el producto puede gestionarlo");
        }
    }

    private void sincronizarEspecies(Producto producto, List<Long> especiesIds) {
        if (especiesIds == null) {
            return;
        }
        if (especiesIds.contains(null)) {
            throw new IllegalArgumentException("El id de la especie es obligatorio");
        }
        Set<Long> idsPedidos = new LinkedHashSet<>(especiesIds);
        producto.getEspecies().removeIf(relacion -> !idsPedidos.contains(relacion.getEspecie().getId()));
        Set<Long> idsActuales = producto.getEspecies().stream()
                .map(relacion -> relacion.getEspecie().getId())
                .collect(Collectors.toSet());

        for (Long especieId : idsPedidos) {
            if (idsActuales.contains(especieId)) {
                continue;
            }
            Especie especie = especieRepository.findById(especieId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especie con id: " + especieId));
            ProductoEspecie relacion = new ProductoEspecie();
            relacion.setId(new ProductoEspecieId(producto.getId(), especie.getId()));
            relacion.setProducto(producto);
            relacion.setEspecie(especie);
            producto.getEspecies().add(relacion);
        }
    }

    private void reemplazarImagenes(Producto producto, List<ImagenProductoRequest> imagenes) {
        if (imagenes == null) {
            return;
        }
        producto.getImagenes().clear();
        boolean hayPrincipal = false;
        int posicion = 1;
        for (ImagenProductoRequest pedido : imagenes) {
            ImagenProducto imagen = new ImagenProducto();
            imagen.setUbicacion(pedido.ubicacion().trim());
            imagen.setPosicion(pedido.posicion() == null ? posicion : pedido.posicion());
            boolean principal = Boolean.TRUE.equals(pedido.principal()) && !hayPrincipal;
            imagen.setPrincipal(principal);
            imagen.setProducto(producto);
            producto.getImagenes().add(imagen);
            hayPrincipal = hayPrincipal || principal;
            posicion++;
        }
        if (!hayPrincipal) {
            producto.getImagenes().get(0).setPrincipal(true);
        }
    }

    private ProductoResponse toResponse(Producto producto) {
        Categoria categoria = producto.getCategoria();
        Marca marca = producto.getMarca();
        Usuario creador = producto.getCreador();

        List<EspecieResponse> especies = producto.getEspecies().stream()
                .map(ProductoEspecie::getEspecie)
                .sorted(Comparator.comparing(Especie::getNombre))
                .map(especie -> new EspecieResponse(especie.getId(), especie.getNombre()))
                .toList();

        List<ImagenProductoResponse> imagenes = producto.getImagenes().stream()
                .sorted(Comparator.comparingInt(ImagenProducto::getPosicion))
                .map(imagen -> new ImagenProductoResponse(imagen.getId(), imagen.getUbicacion(), imagen.getPosicion(),
                        imagen.isPrincipal()))
                .toList();

        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecio(), producto.getStock(), producto.getPublicadoEn(), producto.isDisponible(),
                categoria == null ? null
                        : new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(),
                                categoria.isHabilitada()),
                marca == null ? null : new MarcaResponse(marca.getId(), marca.getNombre(), marca.isHabilitada()),
                creador == null ? null
                        : new CreadorResponse(creador.getId(), creador.getNombreUsuario(), creador.getNombre(),
                                creador.getApellido()),
                especies, imagenes);
    }
}
