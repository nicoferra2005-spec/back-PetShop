package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "marca")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Marca {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(name = "habilitada", nullable = false) private boolean habilitada = true;
    @OneToMany(mappedBy = "marca") private List<Producto> productos = new ArrayList<>();
}
