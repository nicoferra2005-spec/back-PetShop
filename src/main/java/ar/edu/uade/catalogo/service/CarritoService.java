package ar.edu.uade.catalogo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.CarritoResponse;
import ar.edu.uade.catalogo.dto.ItemCarritoRequest;
import ar.edu.uade.catalogo.dto.ItemCarritoResponse;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Carrito;
import ar.edu.uade.catalogo.model.ItemCarrito;
import ar.edu.uade.catalogo.model.Producto;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.CarritoRepository;
import ar.edu.uade.catalogo.repository.ProductoRepository;
import ar.edu.uade.catalogo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CarritoService {
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public CarritoResponse verCarrito(Long usuarioId) {
        return toResponse(obtenerOCrearCarrito(usuarioId));
    }

    public CarritoResponse agregarProducto(Long usuarioId, ItemCarritoRequest request) {
        if (request == null || request.productoId() == null) {
            throw new IllegalArgumentException("El id del producto es obligatorio");
        }
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = obtenerProducto(request.productoId());
        if (!producto.isDisponible()) {
            throw new IllegalArgumentException("El producto no esta disponible");
        }

        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(producto.getId()))
                .findFirst();

        int cantidadFinal = itemExistente.map(ItemCarrito::getCantidad).orElse(0) + request.cantidad();
        validarStockDisponible(producto, cantidadFinal);

        if (itemExistente.isPresent()) {
            itemExistente.get().setCantidad(cantidadFinal);
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(request.cantidad());
            carrito.getItems().add(nuevoItem);
        }

        return toResponse(guardar(carrito));
    }

    public CarritoResponse actualizarCantidad(Long usuarioId, Long itemId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0. Para quitar el producto, usa eliminar");
        }

        Carrito carrito = obtenerCarritoExistente(usuarioId);
        ItemCarrito item = obtenerItem(carrito, itemId);
        validarStockDisponible(item.getProducto(), cantidad);
        item.setCantidad(cantidad);

        return toResponse(guardar(carrito));
    }

    public CarritoResponse eliminarProducto(Long usuarioId, Long itemId) {
        Carrito carrito = obtenerCarritoExistente(usuarioId);
        ItemCarrito item = obtenerItem(carrito, itemId);
        carrito.getItems().remove(item);

        return toResponse(guardar(carrito));
    }

    public CarritoResponse vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoExistente(usuarioId);
        carrito.getItems().clear();

        return toResponse(guardar(carrito));
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Usuario usuario = obtenerUsuario(usuarioId);
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuario(usuario);
            return carritoRepository.save(nuevoCarrito);
        });
    }

    private Carrito obtenerCarritoExistente(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario con id " + usuarioId + " no tiene un carrito"));
    }

    private ItemCarrito obtenerItem(Carrito carrito, Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("El id del item es obligatorio");
        }
        return carrito.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El carrito no tiene un item con id: " + itemId));
    }

    private Producto obtenerProducto(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id: " + productoId));
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El id del usuario es obligatorio");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id: " + usuarioId));
    }

    private void validarStockDisponible(Producto producto, int cantidadPedida) {
        if (cantidadPedida > producto.getStock()) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());
        }
    }

    private Carrito guardar(Carrito carrito) {
        carrito.setActualizadoEn(java.time.LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<ItemCarritoResponse> items = carrito.getItems().stream()
                .map(item -> new ItemCarritoResponse(
                        item.getId(),
                        item.getProducto().getId(),
                        item.getProducto().getNombre(),
                        item.getProducto().getPrecio(),
                        item.getCantidad(),
                        item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()))))
                .toList();

        BigDecimal total = items.stream()
                .map(ItemCarritoResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(carrito.getId(), carrito.getUsuario().getId(), items, total,
                carrito.getActualizadoEn());
    }
}
