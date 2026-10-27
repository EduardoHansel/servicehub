package com.servicehub.servicehubapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Tratamento global de exceções: converte falhas em respostas JSON padronizadas
 * ({@link StandardError}/{@link ValidationError}) e nunca expõe stack traces ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", e.getMessage(), request);
    }

    @ExceptionHandler(EmailAreadyExistsException.class)
    public ResponseEntity<StandardError> emailAlreadyExists(EmailAreadyExistsException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito de dados", e.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardError> businessRule(BusinessRuleException e, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(
                LocalDateTime.now(),
                status.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos",
                request.getRequestURI()
        );

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }
        e.getBindingResult().getGlobalErrors()
                .forEach(g -> err.addError(g.getObjectName(), g.getDefaultMessage()));

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> notReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Requisição malformada",
                "O corpo da requisição é inválido ou contém valores não reconhecidos.", request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, PropertyReferenceException.class})
    public ResponseEntity<StandardError> badParameter(Exception e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "Um ou mais parâmetros da requisição são inválidos.", request);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<StandardError> badCredentials(AuthenticationException e, HttpServletRequest request) {
        // Mensagem genérica para não revelar se o e-mail existe (evita enumeração de usuários)
        return build(HttpStatus.UNAUTHORIZED, "Não autenticado", "Credenciais inválidas.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardError> authentication(AuthenticationException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Não autenticado", "Autenticação necessária.", request);
    }

    /**
     * Negações vindas de {@code @PreAuthorize} ou de checagens de titularidade (BOLA):
     * 401 para requisições anônimas e 403 para usuários autenticados sem permissão.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return build(HttpStatus.UNAUTHORIZED, "Não autenticado", "Autenticação necessária.", request);
        }
        return build(HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para executar esta operação.", request);
    }

    /**
     * Demais exceções. Exceções padrão do Spring MVC (404 de rota, 405, 415...) mantêm seu status;
     * qualquer outra falha vira 500 genérico, sem detalhes internos na resposta.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> generic(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode code = errorResponse.getStatusCode();
            HttpStatus status = HttpStatus.resolve(code.value());
            String reason = status != null ? status.getReasonPhrase() : "Erro";
            return build(code.value(), reason, reason, request);
        }
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<StandardError> build(HttpStatus status, String error, String message,
                                                HttpServletRequest request) {
        return build(status.value(), error, message, request);
    }

    private ResponseEntity<StandardError> build(int status, String error, String message,
                                                HttpServletRequest request) {
        StandardError err = new StandardError(LocalDateTime.now(), status, error, message, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}
