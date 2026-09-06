package ar.edu.uade.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String nombre;
    @Column(length = 1000) private String descripcion;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal precio;
    @Column(nullable = false) private int stock;
    @Column(name = "publicado_en", nullable = false) private LocalDateTime publicadoEn = LocalDateTime.now();
    @Column(name = "disponible", nullable = false) private boolean disponible = true;
    @ManyToOne(optional = false) @JoinColumn(name = "categoria_id") private Categoria categoria;
    @ManyToOne @JoinColumn(name = "marca_id") private Marca marca;
    @ManyToOne(optional = false) @JoinColumn(name = "creador_id") private Usuario creador;
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true) private List<ImagenProducto> imagenes = new ArrayList<>();
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true) private Set<ProductoEspecie> especies = new HashSet<>();
    @OneToMany(mappedBy = "producto") private List<ItemCarrito> itemsEnCarritos = new ArrayList<>();
    @OneToMany(mappedBy = "producto") private List<DetalleCompra> detallesDeCompra = new ArrayList<>();
}
