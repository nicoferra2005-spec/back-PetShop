package ar.edu.uade.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.CompraResponse;
import ar.edu.uade.catalogo.service.CompraService;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping
    public ResponseEntity<List<CompraResponse>> listar(@RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(compraService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.buscarPorId(id));
    }

    @PostMapping("/checkout/{usuarioId}")
    public ResponseEntity<CompraResponse> checkout(@PathVariable Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.checkout(usuarioId));
    }
}
