package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoRepository extends JpaRepository<Producto, Long> { }
