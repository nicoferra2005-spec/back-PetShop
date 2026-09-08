package ar.edu.uade.catalogo.exception;

public class ProductoNoDisponibleException extends RuntimeException {
    public ProductoNoDisponibleException(String message) {
        super(message);
    }
}
