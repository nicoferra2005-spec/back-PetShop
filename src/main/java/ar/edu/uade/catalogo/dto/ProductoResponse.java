package ar.edu.uade.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio, int stock,
        LocalDateTime publicadoEn, boolean disponible, CategoriaResponse categoria, MarcaResponse marca,
        CreadorResponse creador, List<EspecieResponse> especies, List<ImagenProductoResponse> imagenes) {
}
