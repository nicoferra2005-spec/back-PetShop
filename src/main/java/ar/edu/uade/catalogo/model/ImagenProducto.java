package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagen_producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ImagenProducto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "ubicacion", nullable = false, length = 1000) private String ubicacion;
    @Column(name = "posicion", nullable = false) private int posicion = 1;
    @Column(name = "principal", nullable = false) private boolean principal;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
}
