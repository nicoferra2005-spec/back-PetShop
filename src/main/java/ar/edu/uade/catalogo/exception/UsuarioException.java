package ar.edu.uade.catalogo.exception;

// Categoria comun para conflictos de usuario y credenciales invalidas.
public class UsuarioException extends RuntimeException {
    public UsuarioException(String message) {
        super(message);
    }
}
