package ar.edu.uade.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.ActualizarStockRequest;
import ar.edu.uade.catalogo.dto.ProductoRequest;
import ar.edu.uade.catalogo.dto.ProductoResponse;
import ar.edu.uade.catalogo.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Long especieId,
            @RequestParam(required = false) Long creadorId,
            @RequestParam(required = false) String nombre) {
        return productoService.listar(soloDisponibles, categoriaId, marcaId, especieId, creadorId, nombre);
    }

    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @RequestParam Long usuarioId,
            @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request, usuarioId);
    }

    @PatchMapping("/{id}/stock")
    public ProductoResponse actualizarStock(@PathVariable Long id, @RequestParam Long usuarioId,
            @RequestBody ActualizarStockRequest request) {
        return productoService.actualizarStock(id, request, usuarioId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        productoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
