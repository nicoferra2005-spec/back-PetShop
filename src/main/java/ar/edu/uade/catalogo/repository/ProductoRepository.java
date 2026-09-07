package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	@Query("""
			select distinct p from Producto p
			left join p.especies pe
			where (:soloDisponibles = false or p.disponible = true)
			  and (:categoriaId is null or p.categoria.id = :categoriaId)
			  and (:marcaId is null or p.marca.id = :marcaId)
			  and (:especieId is null or pe.especie.id = :especieId)
			  and (:creadorId is null or p.creador.id = :creadorId)
			  and (:nombre is null or lower(p.nombre) like lower(concat('%', :nombre, '%')))
			order by p.nombre asc
			""")
	List<Producto> buscar(@Param("soloDisponibles") boolean soloDisponibles,
			@Param("categoriaId") Long categoriaId,
			@Param("marcaId") Long marcaId,
			@Param("especieId") Long especieId,
			@Param("creadorId") Long creadorId,
			@Param("nombre") String nombre);
}
