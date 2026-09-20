package ar.edu.uade.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La clave es obligatoria")
    private String clave;
}
