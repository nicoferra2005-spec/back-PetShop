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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.MarcaRequest;
import ar.edu.uade.catalogo.dto.MarcaResponse;
import ar.edu.uade.catalogo.service.MarcaService;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {
    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    public List<MarcaResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return marcaService.listar(soloActivas);
    }

    @GetMapping("/{id}")
    public MarcaResponse buscarPorId(@PathVariable Long id) {
        return marcaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<MarcaResponse> crear(@RequestBody MarcaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.crear(request));
    }

    @PutMapping("/{id}")
    public MarcaResponse actualizar(@PathVariable Long id, @RequestBody MarcaRequest request) {
        return marcaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        marcaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
