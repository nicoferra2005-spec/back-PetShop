package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "categoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(length = 500) private String descripcion;
    @Column(name = "habilitada", nullable = false) private boolean habilitada = true;
    @OneToMany(mappedBy = "categoria") private List<Producto> productos = new ArrayList<>();
}
