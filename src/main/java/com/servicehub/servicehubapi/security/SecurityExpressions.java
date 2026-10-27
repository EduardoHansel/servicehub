package com.servicehub.servicehubapi.security;

/**
 * Expressões SpEL reutilizadas em {@code @PreAuthorize}.
 * {@code isAuthenticated()} vem antes de {@code principal.id} para que requisições anônimas
 * (ex.: via /graphql) sejam negadas sem tentar ler o id de um principal inexistente.
 */
public final class SecurityExpressions {

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String ADMIN_OR_PROVIDER = "hasAnyRole('ADMIN', 'PROVIDER')";
    public static final String AUTHENTICATED = "isAuthenticated()";
    public static final String ADMIN_OR_SELF = "hasRole('ADMIN') or (isAuthenticated() and #id == principal.id)";

    private SecurityExpressions() {
    }
}
