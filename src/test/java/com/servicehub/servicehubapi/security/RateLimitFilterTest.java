package com.servicehub.servicehubapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários - RateLimitFilter")
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        RateLimitProperties.Limit loginLimit = new RateLimitProperties.Limit(2, Duration.ofMinutes(1));
        RateLimitProperties.Limit apiLimit = new RateLimitProperties.Limit(5, Duration.ofMinutes(1));
        RateLimitProperties properties = new RateLimitProperties(true, loginLimit, apiLimit);
        JsonErrorResponseWriter errorWriter = new JsonErrorResponseWriter(JsonMapper.builder().build());

        filter = new RateLimitFilter(properties, errorWriter);
    }

    @Test
    @DisplayName("Deve permitir requisições dentro do limite")
    void allowsRequestsWithinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/services");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar HTTP 429 com header Retry-After quando limite for excedido")
    void blocksRequestsExceedingLimit() throws ServletException, IOException {
        String ip = "192.168.1.200";

        // Consome os 2 tokens disponíveis para /api/auth/login
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr(ip);
            filter.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        // 3ª requisição deve ser bloqueada
        MockHttpServletRequest reqBlocked = new MockHttpServletRequest("POST", "/api/auth/login");
        reqBlocked.setRemoteAddr(ip);
        MockHttpServletResponse resBlocked = new MockHttpServletResponse();

        filter.doFilter(reqBlocked, resBlocked, new MockFilterChain());

        assertThat(resBlocked.getStatus()).isEqualTo(429);
        assertThat(resBlocked.getHeader("Retry-After")).isNotNull();
        assertThat(resBlocked.getContentAsString()).contains("Muitas requisições");
    }

    @Test
    @DisplayName("Não deve filtrar rotas que não sejam da API")
    void ignoresNonApiRoutes() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
    }
}
