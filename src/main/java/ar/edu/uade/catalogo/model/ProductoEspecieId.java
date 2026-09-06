package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProductoEspecieId implements Serializable {
    @Column(name = "producto_id") private Long productoId;
    @Column(name = "especie_id") private Long especieId;
}
