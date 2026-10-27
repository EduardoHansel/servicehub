package com.servicehub.servicehubapi.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting por IP com Bucket4j (algoritmo Token Bucket).
 * <ul>
 *   <li>{@code POST /api/auth/login}: limite restrito contra força bruta (OWASP API2);</li>
 *   <li>demais rotas {@code /api/**} e {@code /graphql}: limite geral contra consumo
 *       irrestrito de recursos (OWASP API4).</li>
 * </ul>
 * Excedido o limite, responde HTTP 429 com o header {@code Retry-After}.
 * Os buckets ficam em um cache Caffeine com expiração para não crescer indefinidamente.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    static final String LOGIN_PATH = "/api/auth/login";

    private final RateLimitProperties properties;
    private final JsonErrorResponseWriter errorWriter;
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    public RateLimitFilter(RateLimitProperties properties, JsonErrorResponseWriter errorWriter) {
        this.properties = properties;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.enabled() || !(path.startsWith("/api/") || path.equals("/graphql"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        boolean isLogin = HttpMethod.POST.matches(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
        RateLimitProperties.Limit limit = isLogin ? properties.login() : properties.api();
        String key = (isLogin ? "login:" : "api:") + request.getRemoteAddr();

        Bucket bucket = buckets.get(key, k -> newBucket(limit));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        errorWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                "Muitas requisições",
                "Limite de requisições excedido. Tente novamente em " + retryAfterSeconds + " segundo(s).");
    }

    private static Bucket newBucket(RateLimitProperties.Limit limit) {
        return Bucket.builder()
                .addLimit(l -> l.capacity(limit.capacity()).refillGreedy(limit.capacity(), limit.period()))
                .build();
    }
}
