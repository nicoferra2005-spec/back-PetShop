package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "compra_id") private Compra compra;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private int cantidad;
    @Column(name = "precio", nullable = false, precision = 12, scale = 2) private BigDecimal precio;
}
