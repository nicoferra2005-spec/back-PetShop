package ar.edu.uade.catalogo.exception;

public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
