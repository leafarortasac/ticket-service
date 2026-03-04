package com.br.ticket_service.infrastructure.exception;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR");
    }
}