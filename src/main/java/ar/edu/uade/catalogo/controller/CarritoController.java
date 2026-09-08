package ar.edu.uade.catalogo.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.CantidadRequest;
import ar.edu.uade.catalogo.dto.CarritoResponse;
import ar.edu.uade.catalogo.dto.ItemCarritoRequest;
import ar.edu.uade.catalogo.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{usuarioId}")
    public CarritoResponse verCarrito(@PathVariable Long usuarioId) {
        return carritoService.verCarrito(usuarioId);
    }

    @PostMapping("/{usuarioId}/items")
    public CarritoResponse agregarProducto(@PathVariable Long usuarioId, @RequestBody ItemCarritoRequest request) {
        return carritoService.agregarProducto(usuarioId, request);
    }

    @PutMapping("/{usuarioId}/items/{itemId}")
    public CarritoResponse actualizarCantidad(@PathVariable Long usuarioId, @PathVariable Long itemId,
            @RequestBody CantidadRequest request) {
        return carritoService.actualizarCantidad(usuarioId, itemId, request == null ? null : request.cantidad());
    }

    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public CarritoResponse eliminarProducto(@PathVariable Long usuarioId, @PathVariable Long itemId) {
        return carritoService.eliminarProducto(usuarioId, itemId);
    }

    @DeleteMapping("/{usuarioId}")
    public CarritoResponse vaciarCarrito(@PathVariable Long usuarioId) {
        return carritoService.vaciarCarrito(usuarioId);
    }
}
