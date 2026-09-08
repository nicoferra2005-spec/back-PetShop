package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
  Optional<Carrito> findByUsuarioId(Long usuarioId);
}
