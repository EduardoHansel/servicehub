package com.servicehub.servicehubapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuração do rate limiting ({@code app.rate-limit.*}).
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue Limit login,
        @DefaultValue Limit api
) {
    public record Limit(
            @DefaultValue("10") long capacity,
            @DefaultValue("1m") Duration period
    ) {
    }
}
