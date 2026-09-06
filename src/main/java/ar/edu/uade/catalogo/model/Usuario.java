package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50) private String nombreUsuario;
    @Column(name = "correo", nullable = false, unique = true, length = 150) private String correo;
    @Column(name = "clave", nullable = false, length = 255) private String clave;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 100) private String apellido;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(name = "habilitado", nullable = false) private boolean habilitado = true;
    @OneToMany(mappedBy = "creador") private List<Producto> publicaciones = new ArrayList<>();
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true) private Carrito carrito;
    @OneToMany(mappedBy = "comprador") private List<Compra> compras = new ArrayList<>();
}
