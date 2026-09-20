package ar.edu.uade.catalogo.security;

import ar.edu.uade.catalogo.model.Rol;
import ar.edu.uade.catalogo.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapta la entidad {@link Usuario} al contrato que Spring Security espera.
 *
 * <p>Se mantiene como una clase aparte, y no anotando la entidad, para que el
 * modelo de dominio no dependa de Spring Security.</p>
 */
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getId() {
        return usuario.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Rol> roles = usuario.getRoles();

        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return usuario.getClave();
    }

    /**
     * El correo es el identificador con el que se autentica el usuario, porque es
     * el dato que ya recibe {@code LoginRequest}.
     */
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isHabilitado();
    }
}
