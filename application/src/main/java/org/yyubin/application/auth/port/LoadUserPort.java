package org.yyubin.application.auth.port;

import org.yyubin.domain.user.User;

public interface LoadUserPort {
    User loadByEmail(String email);
}
