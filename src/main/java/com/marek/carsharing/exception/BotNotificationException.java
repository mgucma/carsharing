package com.marek.carsharing.exception;

public class BotNotificationException extends RuntimeException {
    public BotNotificationException(String message) {
        super(message);
    }

    public BotNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BotNotificationException(Throwable cause) {
        super(cause);
    }
}

