package org.yyubin.application.user.port;

import java.util.List;
import java.util.Map;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

public interface LoadUserPort {
    User loadById(UserId userId);
    User loadByEmail(String email);
    Map<Long, User> loadByIdsBatch(List<Long> userIds);
}
