package ar.edu.uade.catalogo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/checkout/{usuarioId}")
    public ResponseEntity<CompraResponse> checkout(@PathVariable Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.checkout(usuarioId));
    }
}
