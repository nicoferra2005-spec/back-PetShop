package ar.edu.uade.catalogo.model;

/**
 * Roles que puede tener un usuario.
 *
 * <p>Los nombres llevan el prefijo {@code ROLE_} que espera Spring Security,
 * de modo que el enum se mapea directo a una autoridad sin traducciones
 * intermedias.</p>
 */
public enum Rol {
    ROLE_CLIENTE,
    ROLE_ADMIN
}
