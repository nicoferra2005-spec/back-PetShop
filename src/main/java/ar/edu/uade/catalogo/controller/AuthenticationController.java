package ar.edu.uade.catalogo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.uade.catalogo.dto.LoginRequest;
import ar.edu.uade.catalogo.dto.RegisterRequest;
import ar.edu.uade.catalogo.dto.UsuarioResponseDTO;
import ar.edu.uade.catalogo.service.AuthenticationService;
import jakarta.validation.Valid;

// http://localhost:8080/api/auth
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // post http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UsuarioResponseDTO usuarioRegistrado = authenticationService.registrar(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRegistrado);
    }

    // post http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsuarioResponseDTO usuarioLogueado = authenticationService.login(loginRequest);
        return ResponseEntity.ok().body(usuarioLogueado);
    }
}
