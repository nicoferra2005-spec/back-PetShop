package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductoEspecie {
    @EmbeddedId private ProductoEspecieId id;
    @ManyToOne(optional = false) @MapsId("productoId") @JoinColumn(name = "producto_id") private Producto producto;
    @ManyToOne(optional = false) @MapsId("especieId") @JoinColumn(name = "especie_id") private Especie especie;
}
