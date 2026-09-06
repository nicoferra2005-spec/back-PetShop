package ar.edu.uade.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String nombreUsuario;
    private String correo;
    private String clave;
    private String nombre;
    private String apellido;
}
