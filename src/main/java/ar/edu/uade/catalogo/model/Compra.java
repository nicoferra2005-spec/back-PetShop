package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "compra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Compra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "usuario_id") private Usuario comprador;
    @Column(name = "realizada_en", nullable = false) private LocalDateTime realizadaEn = LocalDateTime.now();
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total = BigDecimal.ZERO;
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true) private List<DetalleCompra> lineas = new ArrayList<>();
}
