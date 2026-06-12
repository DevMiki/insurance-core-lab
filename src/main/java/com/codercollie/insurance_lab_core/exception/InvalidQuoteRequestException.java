package com.codercollie.insurance_lab_core.exception;

public class InvalidQuoteRequestException extends RuntimeException {

    public InvalidQuoteRequestException(String message) {
        super(message);
    }
}
