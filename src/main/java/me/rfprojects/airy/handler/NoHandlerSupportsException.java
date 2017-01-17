package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.AiryException;

public class NoHandlerSupportsException extends AiryException {

    private static final long serialVersionUID = -4374047677336582509L;

    public NoHandlerSupportsException() {
    }

    public NoHandlerSupportsException(String message) {
        super(message);
    }

    public NoHandlerSupportsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoHandlerSupportsException(Throwable cause) {
        super(cause);
    }
}
