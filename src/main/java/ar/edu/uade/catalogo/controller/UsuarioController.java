package ar.edu.uade.catalogo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.UsuarioResponseDTO;
import ar.edu.uade.catalogo.service.UsuarioService;

// http://localhost:8080/api/usuarios
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // get http://localhost:8080/api/usuarios
    @GetMapping()
    public ResponseEntity<List<UsuarioResponseDTO>> findAllUsuarios() {
        return ResponseEntity.ok(usuarioService.findAllUsuarios());
    }

    // get http://localhost:8080/api/usuarios/1
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findUsuarioById(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.findUsuarioById(id);
        return ResponseEntity.ok().body(usuario);
    }

    // get http://localhost:8080/api/usuarios/buscar?correo=juan@mail.com
    @GetMapping("/buscar")
    public ResponseEntity<UsuarioResponseDTO> findUsuarioByCorreo(@RequestParam String correo) {
        UsuarioResponseDTO usuario = usuarioService.findUsuarioByCorreo(correo);
        return ResponseEntity.ok().body(usuario);
    }
}
