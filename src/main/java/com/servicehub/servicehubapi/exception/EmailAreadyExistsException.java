package com.servicehub.servicehubapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class EmailAreadyExistsException extends RuntimeException {
    public EmailAreadyExistsException(String message) {
        super(message);
    }
}
