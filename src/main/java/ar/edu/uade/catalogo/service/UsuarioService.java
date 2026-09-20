package ar.edu.uade.catalogo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.LoginRequest;
import ar.edu.uade.catalogo.dto.RegisterRequest;
import ar.edu.uade.catalogo.dto.UsuarioResponseDTO;
import ar.edu.uade.catalogo.exception.CorreoYaRegistradoException;
import ar.edu.uade.catalogo.exception.CredencialesInvalidasException;
import ar.edu.uade.catalogo.exception.DatosInvalidosException;
import ar.edu.uade.catalogo.exception.NombreUsuarioYaRegistradoException;
import ar.edu.uade.catalogo.exception.UsuarioNotFoundException;
import ar.edu.uade.catalogo.model.Rol;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> findAllUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public UsuarioResponseDTO registrarUsuario(RegisterRequest registerRequest) {
        if (registerRequest.getCorreo() == null || registerRequest.getClave() == null
                || registerRequest.getNombreUsuario() == null) {
            throw new DatosInvalidosException("Correo, clave y nombre de usuario son obligatorios");
        }

        if (usuarioRepository.existsByCorreo(registerRequest.getCorreo())) {
            throw new CorreoYaRegistradoException("Ya existe un usuario registrado con ese correo");
        }

        if (usuarioRepository.existsByNombreUsuario(registerRequest.getNombreUsuario())) {
            throw new NombreUsuarioYaRegistradoException("Ya existe un usuario registrado con ese nombre de usuario");
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(registerRequest.getNombreUsuario())
                .correo(registerRequest.getCorreo())
                // La clave nunca se persiste en texto plano.
                .clave(passwordEncoder.encode(registerRequest.getClave()))
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .roles(new HashSet<>(Set.of(Rol.ROLE_CLIENTE)))
                .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return toResponseDTO(savedUsuario);
    }

    public UsuarioResponseDTO login(LoginRequest loginRequest) {
        if (loginRequest.getCorreo() == null || loginRequest.getClave() == null) {
            throw new DatosInvalidosException("Correo y clave son obligatorios");
        }

        Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getCorreo())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o clave incorrectos"));

        if (!passwordEncoder.matches(loginRequest.getClave(), usuario.getClave())) {
            throw new CredencialesInvalidasException("Correo o clave incorrectos");
        }

        if (!usuario.isHabilitado()) {
            throw new CredencialesInvalidasException("El usuario se encuentra deshabilitado");
        }

        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO findUsuarioByCorreo(String correo) {
        if (correo == null) {
            throw new DatosInvalidosException("El correo no puede ser nulo");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNotFoundException("No existe un usuario con el correo: " + correo));

        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO findUsuarioById(Long id) {
        if (id == null) {
            throw new DatosInvalidosException("El id del usuario no puede ser nulo");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("No existe un usuario con el id: " + id));

        return toResponseDTO(usuario);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreUsuario(usuario.getNombreUsuario())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .habilitado(usuario.isHabilitado())
                .roles(usuario.getRoles() == null ? Set.of() : Set.copyOf(usuario.getRoles()))
                .build();
    }
}
