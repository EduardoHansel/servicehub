package com.servicehub.servicehubapi.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");

    @Test
    void handleResourceNotFound() {
        ResponseEntity<StandardError> response = handler.resourceNotFound(
                new ResourceNotFoundException("Não encontrado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("Recurso não encontrado");
    }

    @Test
    void handleEmailAlreadyExists() {
        ResponseEntity<StandardError> response = handler.emailAlreadyExists(
                new EmailAreadyExistsException("Email já existe"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("Conflito de dados");
    }

    @Test
    void handleBusinessRule() {
        ResponseEntity<StandardError> response = handler.businessRule(
                new BusinessRuleException("Regra violada"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody().getError()).isEqualTo("Regra de negócio violada");
    }

    @Test
    void handleBadCredentials() {
        ResponseEntity<StandardError> response = handler.badCredentials(
                new BadCredentialsException("Senha errada"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Credenciais inválidas.");
    }

    @Test
    void handleAccessDenied() {
        ResponseEntity<StandardError> response = handler.accessDenied(
                new AccessDeniedException("Sem permissão"), request);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void handleGenericException() {
        ResponseEntity<StandardError> response = handler.generic(
                new RuntimeException("Falha inesperada"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("Erro interno");
    }
}
