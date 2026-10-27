package com.servicehub.servicehubapi.security;

import com.servicehub.servicehubapi.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/**
 * Geração e validação de JSON Web Tokens (RFC 7519) assinados com HMAC-SHA256 (HS256).
 * <p>
 * Claims emitidos: {@code sub} (e-mail), {@code uid} (id do usuário), {@code role},
 * {@code iss}, {@code iat} e {@code exp}.
 */
@Service
public class JwtService {

    static final String CLAIM_USER_ID = "uid";
    static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
                      @Value("${app.jwt.issuer:servicehub-api}") String issuer) {
        // Keys.hmacShaKeyFor exige chave >= 256 bits para HS256 (falha rápido se for fraca)
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.email())
                .claim(CLAIM_USER_ID, principal.id())
                .claim(CLAIM_ROLE, principal.role().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida assinatura, emissor e expiração. Retorna vazio para qualquer token inválido.
     */
    public Optional<UserPrincipal> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get(CLAIM_USER_ID, Long.class);
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            return Optional.of(UserPrincipal.fromToken(userId, claims.getSubject(), role));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
