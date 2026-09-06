package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "especie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Especie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String nombre;
    @OneToMany(mappedBy = "especie") private Set<ProductoEspecie> productos = new HashSet<>();
}
