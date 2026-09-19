package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    Optional<ItemCarrito> findFirstByCarrito_IdAndProducto_Id(Long carritoId, Long productoId);

    Optional<ItemCarrito> findByIdAndCarrito_Id(Long itemId, Long carritoId);
}
