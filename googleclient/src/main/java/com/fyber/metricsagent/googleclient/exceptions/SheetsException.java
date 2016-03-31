package com.fyber.metricsagent.googleclient.exceptions;

public class SheetsException extends Exception {
    public SheetsException() {}

    public SheetsException(String message) {
        super(message);
    }

    public SheetsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SheetsException(Throwable cause) {
        super(cause);
    }

    public SheetsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
