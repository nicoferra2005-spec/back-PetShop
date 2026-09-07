package ar.edu.uade.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.CategoriaRequest;
import ar.edu.uade.catalogo.dto.CategoriaResponse;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Categoria;
import ar.edu.uade.catalogo.repository.CategoriaRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar(Boolean soloActivas) {
        List<Categoria> categorias = Boolean.TRUE.equals(soloActivas)
                ? categoriaRepository.findByHabilitadaTrueOrderByNombreAsc()
                : categoriaRepository.findAllByOrderByNombreAsc();
        return categorias.stream().map(this::toResponse).toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return toResponse(obtener(id));
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        validarNombre(request.nombre());
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        categoria.setHabilitada(request.habilitada() == null || request.habilitada());
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        validarNombre(request.nombre());
        Categoria categoria = obtener(id);
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        if (request.habilitada() != null) {
            categoria.setHabilitada(request.habilitada());
        }
        return toResponse(categoriaRepository.save(categoria));
    }

    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la categoria es obligatorio");
        }
        obtener(id);
        categoriaRepository.deleteById(id);
    }

    private Categoria obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la categoria es obligatorio");
        }
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria con id: " + id));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio");
        }
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.isHabilitada());
    }
}
