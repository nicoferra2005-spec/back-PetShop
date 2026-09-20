package ar.edu.uade.catalogo.exception;

// Conserva la compatibilidad con las validaciones que usaban IllegalArgumentException.
public class DatosInvalidosException extends IllegalArgumentException {
    public DatosInvalidosException(String message) {
        super(message);
    }
}
