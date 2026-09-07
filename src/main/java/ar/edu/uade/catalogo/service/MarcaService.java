package ar.edu.uade.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.uade.catalogo.dto.MarcaRequest;
import ar.edu.uade.catalogo.dto.MarcaResponse;
import ar.edu.uade.catalogo.exception.RecursoNoEncontradoException;
import ar.edu.uade.catalogo.model.Marca;
import ar.edu.uade.catalogo.repository.MarcaRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class MarcaService {
    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    public List<MarcaResponse> listar(Boolean soloActivas) {
        List<Marca> marcas = Boolean.TRUE.equals(soloActivas)
                ? marcaRepository.findByHabilitadaTrueOrderByNombreAsc()
                : marcaRepository.findAllByOrderByNombreAsc();
        return marcas.stream().map(this::toResponse).toList();
    }

    public MarcaResponse buscarPorId(Long id) {
        return toResponse(obtener(id));
    }

    public MarcaResponse crear(MarcaRequest request) {
        validarNombre(request.nombre());
        Marca marca = new Marca();
        marca.setNombre(request.nombre().trim());
        marca.setHabilitada(request.habilitada() == null || request.habilitada());
        return toResponse(marcaRepository.save(marca));
    }

    public MarcaResponse actualizar(Long id, MarcaRequest request) {
        validarNombre(request.nombre());
        Marca marca = obtener(id);
        marca.setNombre(request.nombre().trim());
        if (request.habilitada() != null) {
            marca.setHabilitada(request.habilitada());
        }
        return toResponse(marcaRepository.save(marca));
    }

    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la marca es obligatorio");
        }
        obtener(id);
        marcaRepository.deleteById(id);
    }

    private Marca obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la marca es obligatorio");
        }
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la marca con id: " + id));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la marca es obligatorio");
        }
    }

    private MarcaResponse toResponse(Marca marca) {
        return new MarcaResponse(marca.getId(), marca.getNombre(), marca.isHabilitada());
    }
}
