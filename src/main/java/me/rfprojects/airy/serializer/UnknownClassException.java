package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.AiryException;

public class UnknownClassException extends AiryException {

    private static final long serialVersionUID = -1617704353436719113L;

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
