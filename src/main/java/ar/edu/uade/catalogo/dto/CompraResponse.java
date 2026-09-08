package ar.edu.uade.catalogo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponse(Long id, Long usuarioId, LocalDateTime realizadaEn, BigDecimal total,
        List<DetalleCompraResponse> lineas) {
}
