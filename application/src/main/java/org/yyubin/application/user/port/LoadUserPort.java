package org.yyubin.application.user.port;

import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

public interface LoadUserPort {
    User loadById(UserId userId);
    User loadByEmail(String email);
}
