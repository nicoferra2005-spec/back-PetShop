package ar.edu.uade.catalogo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarritoResponse(Long id, Long usuarioId, List<ItemCarritoResponse> items, BigDecimal total, LocalDateTime actualizadoEn){
  
}
