package ar.edu.uade.catalogo.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.UsuarioResponseDTO;
import ar.edu.uade.catalogo.exception.DatosInvalidosException;
import ar.edu.uade.catalogo.exception.UsuarioNotFoundException;
import ar.edu.uade.catalogo.model.Usuario;
import ar.edu.uade.catalogo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponseDTO> findAllUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toResponseDTO).toList();
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
