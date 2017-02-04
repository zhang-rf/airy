package me.rfprojects.airy.core;

public class AiryException extends RuntimeException {

    private static final long serialVersionUID = -6712243565983610752L;

    public AiryException() {
    }

    public AiryException(String message) {
        super(message);
    }

    public AiryException(String message, Throwable cause) {
        super(message, cause);
    }

    public AiryException(Throwable cause) {
        super(cause);
    }
}
