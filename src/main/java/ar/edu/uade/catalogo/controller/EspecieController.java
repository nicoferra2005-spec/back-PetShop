package ar.edu.uade.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.EspecieRequest;
import ar.edu.uade.catalogo.dto.EspecieResponse;
import ar.edu.uade.catalogo.service.EspecieService;

@RestController
@RequestMapping("/api/especies")
public class EspecieController {
    private final EspecieService especieService;

    public EspecieController(EspecieService especieService) {
        this.especieService = especieService;
    }

    @GetMapping
    public List<EspecieResponse> listar() {
        return especieService.listar();
    }

    @GetMapping("/{id}")
    public EspecieResponse buscarPorId(@PathVariable Long id) {
        return especieService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<EspecieResponse> crear(@RequestBody EspecieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especieService.crear(request));
    }

    @PutMapping("/{id}")
    public EspecieResponse actualizar(@PathVariable Long id, @RequestBody EspecieRequest request) {
        return especieService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especieService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
