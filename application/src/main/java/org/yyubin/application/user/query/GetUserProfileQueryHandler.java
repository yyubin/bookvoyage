package org.yyubin.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class GetUserProfileQueryHandler {

    private final LoadUserPort loadUserPort;

    public UserProfileResult handle(GetUserProfileQuery query) {
        User user = loadUserPort.loadById(new UserId(query.userId()));
        return UserProfileResult.from(user);
    }
}
