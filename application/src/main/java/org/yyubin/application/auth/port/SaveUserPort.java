package org.yyubin.application.auth.port;

import org.yyubin.domain.user.User;

public interface SaveUserPort {
    User save(User user);
}
