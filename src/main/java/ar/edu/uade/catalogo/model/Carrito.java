package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "carrito")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Carrito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "usuario_id", unique = true) private Usuario usuario;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name = "actualizado_en", nullable = false) private LocalDateTime actualizadoEn = LocalDateTime.now();
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true) private List<ItemCarrito> items = new ArrayList<>();
}
