package org.yyubin.domain.user;

public class InvalidPasswordException extends IllegalArgumentException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
