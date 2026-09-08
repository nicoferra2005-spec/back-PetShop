package ar.edu.uade.catalogo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.CompraResponse;
import ar.edu.uade.catalogo.dto.DetalleCompraResponse;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Carrito;
import ar.edu.uade.catalogo.model.Compra;
import ar.edu.uade.catalogo.model.DetalleCompra;
import ar.edu.uade.catalogo.model.ItemCarrito;
import ar.edu.uade.catalogo.model.Producto;
import ar.edu.uade.catalogo.repository.CarritoRepository;
import ar.edu.uade.catalogo.repository.CompraRepository;
import ar.edu.uade.catalogo.repository.ProductoRepository;
import jakarta.transaction.Transactional;

@Service
public class CompraService {
    private final CarritoRepository carritoRepository;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;

    public CompraService(CarritoRepository carritoRepository, CompraRepository compraRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.compraRepository = compraRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public CompraResponse checkout(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El id del usuario es obligatorio");
        }

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario con id " + usuarioId + " no tiene un carrito"));
        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito debe tener al menos un producto para realizar la compra");
        }

        Compra compra = new Compra();
        compra.setComprador(carrito.getUsuario());
        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe el producto con id: " + item.getProducto().getId()));
            int cantidad = item.getCantidad();
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (!producto.isDisponible()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no esta disponible");
            }
            if (cantidad > producto.getStock()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());
            }

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            total = total.add(subtotal);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecio(precioUnitario);
            compra.getLineas().add(detalle);

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }

        compra.setTotal(total);
        Compra guardada = compraRepository.save(compra);

        carrito.getItems().clear();
        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);

        return toResponse(guardada);
    }

    private CompraResponse toResponse(Compra compra) {
        List<DetalleCompraResponse> lineas = compra.getLineas().stream()
                .map(detalle -> new DetalleCompraResponse(
                        detalle.getId(),
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getPrecio(),
                        detalle.getCantidad(),
                        detalle.getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad()))))
                .toList();

        return new CompraResponse(compra.getId(), compra.getComprador().getId(), compra.getRealizadaEn(),
                compra.getTotal(), lineas);
    }
}
