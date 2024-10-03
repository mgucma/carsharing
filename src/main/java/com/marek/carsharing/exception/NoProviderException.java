package com.marek.carsharing.exception;

public class NoProviderException extends RuntimeException {
    public NoProviderException(String message) {
        super(message);
    }

    public NoProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProviderException(Throwable cause) {
        super(cause);
    }
}

