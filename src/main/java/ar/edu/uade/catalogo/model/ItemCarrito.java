package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_carrito")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemCarrito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "carrito_id") private Carrito carrito;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private int cantidad;
    @Column(name = "agregado_en", nullable = false) private LocalDateTime agregadoEn = LocalDateTime.now();
}
