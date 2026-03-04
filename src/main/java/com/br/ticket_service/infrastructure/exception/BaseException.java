package com.br.ticket_service.infrastructure.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String code;

    protected BaseException(String message, String code) {
        super(message);
        this.code = code;
    }
}
