package ar.edu.uade.catalogo.dto;

import java.math.BigDecimal;

public record DetalleCompraResponse(Long id, Long productoId, String productoNombre, BigDecimal precioUnitario,
        int cantidad, BigDecimal subtotal) {
}
