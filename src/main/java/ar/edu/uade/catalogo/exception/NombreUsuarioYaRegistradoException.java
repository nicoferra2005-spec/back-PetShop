package ar.edu.uade.catalogo.exception;

public class NombreUsuarioYaRegistradoException extends RuntimeException {
    public NombreUsuarioYaRegistradoException(String message) {
        super(message);
    }
}
