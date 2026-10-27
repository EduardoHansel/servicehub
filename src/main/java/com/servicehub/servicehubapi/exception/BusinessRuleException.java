package com.servicehub.servicehubapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Violação de regra de negócio (dados sintaticamente válidos, mas não processáveis).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT) // 422
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
