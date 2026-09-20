package ar.edu.uade.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ar.edu.uade.catalogo.dto.ItemCarritoRequest;
import ar.edu.uade.catalogo.exception.DatosInvalidosException;
import ar.edu.uade.catalogo.exception.ProductoNoDisponibleException;
import ar.edu.uade.catalogo.exception.StockInsuficienteException;
import ar.edu.uade.catalogo.model.Carrito;
import ar.edu.uade.catalogo.model.ItemCarrito;
import ar.edu.uade.catalogo.model.Producto;
import ar.edu.uade.catalogo.repository.CarritoRepository;
import ar.edu.uade.catalogo.repository.ItemCarritoRepository;
import ar.edu.uade.catalogo.repository.ProductoRepository;
import ar.edu.uade.catalogo.repository.UsuarioRepository;
import ar.edu.uade.catalogo.service.CarritoService;

class CarritoServiceExcepcionesTest {
    private final CarritoRepository carritos = mock(CarritoRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ItemCarritoRepository items = mock(ItemCarritoRepository.class);
    private final CarritoService service = new CarritoService(carritos, productos, usuarios, items);

    @Test
    void cantidadInvalidaSeRechazaAntesDeAccederALosRepositorios() {
        assertThatThrownBy(() -> service.agregarProducto(1L, new ItemCarritoRequest(10L, 0)))
                .isExactlyInstanceOf(DatosInvalidosException.class)
                .hasMessage("La cantidad debe ser mayor a 0");

        verifyNoInteractions(carritos, productos, usuarios, items);
    }

    @Test
    void productoNoDisponibleSeRechazaAntesDeCrearElCarrito() {
        Producto producto = new Producto();
        producto.setDisponible(false);
        when(productos.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> service.agregarProducto(1L, new ItemCarritoRequest(10L, 1)))
                .isExactlyInstanceOf(ProductoNoDisponibleException.class)
                .hasMessage("El producto no esta disponible");

        verifyNoInteractions(carritos, usuarios, items);
    }

    @Test
    void stockInsuficienteConsideraLaCantidadAcumuladaSinModificarElCarrito() {
        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Alimento");
        producto.setStock(3);
        Carrito carrito = new Carrito();
        carrito.setId(20L);
        ItemCarrito item = new ItemCarrito();
        item.setProducto(producto);
        item.setCarrito(carrito);
        item.setCantidad(2);
        carrito.getItems().add(item);
        when(productos.findById(10L)).thenReturn(Optional.of(producto));
        when(carritos.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(items.findFirstByCarrito_IdAndProducto_Id(20L, 10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.agregarProducto(1L, new ItemCarritoRequest(10L, 2)))
                .isExactlyInstanceOf(StockInsuficienteException.class)
                .hasMessage("Stock insuficiente para Alimento. Disponible: 3");

        assertThat(item.getCantidad()).isEqualTo(2);
        assertThat(producto.getStock()).isEqualTo(3);
        verify(carritos, never()).save(any(Carrito.class));
    }
}
