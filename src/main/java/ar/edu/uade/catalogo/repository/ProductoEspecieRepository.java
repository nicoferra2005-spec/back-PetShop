package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.ProductoEspecie;
import ar.edu.uade.catalogo.model.ProductoEspecieId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoEspecieRepository extends JpaRepository<ProductoEspecie, ProductoEspecieId> { }
