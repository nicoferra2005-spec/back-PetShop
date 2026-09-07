package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EspecieRepository extends JpaRepository<Especie, Long> {
	List<Especie> findAllByOrderByNombreAsc();
	boolean existsByNombreIgnoreCase(String nombre);
}
