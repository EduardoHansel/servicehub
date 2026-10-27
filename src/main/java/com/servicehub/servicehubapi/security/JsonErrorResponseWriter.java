package com.servicehub.servicehubapi.security;

import com.servicehub.servicehubapi.exception.StandardError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Escreve respostas de erro no mesmo formato JSON do {@code GlobalExceptionHandler}
 * para falhas que acontecem nos filtros (antes de chegar aos controllers):
 * 401, 403 e 429.
 */
@Component
public class JsonErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public JsonErrorResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String error, String message) throws IOException {
        StandardError body = new StandardError(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}
