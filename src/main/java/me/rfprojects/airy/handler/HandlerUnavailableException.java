package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.AiryException;

public class HandlerUnavailableException extends AiryException {

    private static final long serialVersionUID = -4374047677336582509L;

    public HandlerUnavailableException() {
    }

    public HandlerUnavailableException(String message) {
        super(message);
    }

    public HandlerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerUnavailableException(Throwable cause) {
        super(cause);
    }
}
