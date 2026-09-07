package ar.edu.uade.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(String nombre, String descripcion, BigDecimal precio, Integer stock, Long categoriaId,
        Long marcaId, Long creadorId, Boolean disponible, List<Long> especiesIds,
        List<ImagenProductoRequest> imagenes) {
}
