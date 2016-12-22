package me.rfprojects.airy.core;

public class UnknownClassException extends RuntimeException {

    public UnknownClassException() {
    }

    public UnknownClassException(String message) {
        super(message);
    }

    public UnknownClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownClassException(Throwable cause) {
        super(cause);
    }
}
