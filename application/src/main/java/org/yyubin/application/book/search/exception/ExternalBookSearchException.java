package org.yyubin.application.book.search.exception;

public class ExternalBookSearchException extends RuntimeException {
    public ExternalBookSearchException(String message) {
        super(message);
    }

    public ExternalBookSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
