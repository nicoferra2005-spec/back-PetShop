package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
	List<Categoria> findAllByOrderByNombreAsc();
	List<Categoria> findByHabilitadaTrueOrderByNombreAsc();
}
