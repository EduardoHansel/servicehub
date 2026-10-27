package com.servicehub.servicehubapi.security;

import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Representa o usuário autenticado no {@code SecurityContext}.
 * <p>
 * É criado pelo {@link UserDetailsServiceImpl} durante o login (com o hash da senha)
 * e pelo {@link JwtAuthFilter} a cada requisição (somente com os claims do token,
 * sem consultar o banco - autenticação stateless).
 */
public record UserPrincipal(Long id, String email, String password, Role role, boolean enabled)
        implements UserDetails {

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive())
        );
    }

    public static UserPrincipal fromToken(Long id, String email, Role role) {
        return new UserPrincipal(id, email, null, role, true);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        // Nunca expõe o hash da senha em logs
        return "UserPrincipal[id=" + id + ", email=" + email + ", role=" + role + "]";
    }
}
