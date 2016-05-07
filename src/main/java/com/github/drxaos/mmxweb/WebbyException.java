package com.github.drxaos.mmxweb;

public class WebbyException extends RuntimeException {
    public WebbyException() {
    }

    public WebbyException(String message) {
        super(message);
    }

    public WebbyException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebbyException(Throwable cause) {
        super(cause);
    }
}
