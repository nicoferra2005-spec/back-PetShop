package ar.edu.uade.catalogo.dto;

import ar.edu.uade.catalogo.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombreUsuario;
    private String correo;
    private String nombre;
    private String apellido;
    private boolean habilitado;
    private Set<Rol> roles;
}
