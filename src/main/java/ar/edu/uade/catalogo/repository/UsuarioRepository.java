package ar.edu.uade.catalogo.repository;
import ar.edu.uade.catalogo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { }
