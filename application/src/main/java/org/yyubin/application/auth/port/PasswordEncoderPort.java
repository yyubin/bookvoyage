package org.yyubin.application.auth.port;

public interface PasswordEncoderPort {
    boolean matches(String rawPassword, String hashedPassword);
    String encode(String rawPassword);
}
