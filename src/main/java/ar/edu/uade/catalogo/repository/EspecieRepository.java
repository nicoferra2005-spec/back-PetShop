package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EspecieRepository extends JpaRepository<Especie, Long> { }
