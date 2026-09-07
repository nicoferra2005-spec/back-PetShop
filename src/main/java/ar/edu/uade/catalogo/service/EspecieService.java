package ar.edu.uade.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.EspecieRequest;
import ar.edu.uade.catalogo.dto.EspecieResponse;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Especie;
import ar.edu.uade.catalogo.repository.EspecieRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class EspecieService {
    private final EspecieRepository especieRepository;

    public EspecieService(EspecieRepository especieRepository) {
        this.especieRepository = especieRepository;
    }

    public List<EspecieResponse> listar() {
        return especieRepository.findAllByOrderByNombreAsc().stream().map(this::toResponse).toList();
    }

    public EspecieResponse buscarPorId(Long id) {
        return toResponse(obtener(id));
    }

    public EspecieResponse crear(EspecieRequest request) {
        validarNombre(request.nombre());
        String nombre = request.nombre().trim();
        if (especieRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una especie con ese nombre");
        }
        Especie especie = new Especie();
        especie.setNombre(nombre);
        return toResponse(especieRepository.save(especie));
    }

    public EspecieResponse actualizar(Long id, EspecieRequest request) {
        validarNombre(request.nombre());
        Especie especie = obtener(id);
        String nombre = request.nombre().trim();
        if (!especie.getNombre().equalsIgnoreCase(nombre) && especieRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una especie con ese nombre");
        }
        especie.setNombre(nombre);
        return toResponse(especieRepository.save(especie));
    }

    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la especie es obligatorio");
        }
        obtener(id);
        especieRepository.deleteById(id);
    }

    private Especie obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la especie es obligatorio");
        }
        return especieRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la especie con id: " + id));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la especie es obligatorio");
        }
    }

    private EspecieResponse toResponse(Especie especie) {
        return new EspecieResponse(especie.getId(), especie.getNombre());
    }
}
