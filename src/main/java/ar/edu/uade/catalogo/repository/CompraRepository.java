package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByRealizadaEnDesc();
    List<Compra> findByCompradorIdOrderByRealizadaEnDesc(Long compradorId);
}
