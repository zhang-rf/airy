package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.AiryException;

public class NotInitializedException extends AiryException {

    private static final long serialVersionUID = 5274963798581000317L;

    public NotInitializedException() {
    }

    public NotInitializedException(String message) {
        super(message);
    }

    public NotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotInitializedException(Throwable cause) {
        super(cause);
    }
}
