package ar.edu.uade.catalogo.dto;
import java.math.BigDecimal;
public record ItemCarritoResponse(Long id, Long productoId, String productoNombre, BigDecimal precioUnitario,int cantidad, BigDecimal subtotal){
  
}
