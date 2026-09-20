package ar.edu.uade.catalogo.exception;

public class UsuarioNotFoundException extends RecursoNoEncontradoException {
    public UsuarioNotFoundException(String message) {
        super(message);
    }
}
