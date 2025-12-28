package org.yyubin.application.user;

import org.yyubin.application.user.dto.CheckFollowStatusResult;
import org.yyubin.application.user.query.CheckFollowStatusQuery;

public interface CheckFollowStatusUseCase {

    CheckFollowStatusResult check(CheckFollowStatusQuery query);
}
