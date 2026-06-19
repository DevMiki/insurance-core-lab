package com.codercollie.insurance_lab_core.exception;

public class InvalidClaimRequestException extends RuntimeException {
    public InvalidClaimRequestException(String message) {
        super(message);
    }
}
