package org.yyubin.application.auth.port;

import java.util.Optional;
import org.yyubin.domain.user.User;

public interface LoadUserPort {
    User loadByEmail(String email);
    Optional<User> loadByNickname(String nickname);
}
