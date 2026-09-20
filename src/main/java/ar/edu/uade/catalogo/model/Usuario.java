package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50) private String nombreUsuario;
    @Column(name = "correo", nullable = false, unique = true, length = 150) private String correo;
    @Column(name = "clave", nullable = false, length = 255) private String clave;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 100) private String apellido;
    @Builder.Default @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Builder.Default @Column(name = "habilitado", nullable = false) private boolean habilitado = true;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Set<Rol> roles = new HashSet<>();

    @Builder.Default @OneToMany(mappedBy = "creador") private List<Producto> publicaciones = new ArrayList<>();
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true) private Carrito carrito;
    @Builder.Default @OneToMany(mappedBy = "comprador") private List<Compra> compras = new ArrayList<>();

    /** Agrega un rol conservando la invariante de que {@code roles} nunca es null. */
    public void agregarRol(Rol rol) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(rol);
    }
}
