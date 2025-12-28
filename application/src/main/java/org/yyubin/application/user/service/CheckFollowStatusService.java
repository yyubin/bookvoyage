package org.yyubin.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.user.CheckFollowStatusUseCase;
import org.yyubin.application.user.dto.CheckFollowStatusResult;
import org.yyubin.application.user.port.FollowPort;
import org.yyubin.application.user.query.CheckFollowStatusQuery;

@Service
@RequiredArgsConstructor
public class CheckFollowStatusService implements CheckFollowStatusUseCase {

    private final FollowPort followPort;

    @Override
    @Transactional(readOnly = true)
    public CheckFollowStatusResult check(CheckFollowStatusQuery query) {
        // 본인 프로필일 때는 무조건 false 반환
        if (query.followerId().equals(query.targetUserId())) {
            return new CheckFollowStatusResult(false);
        }

        boolean following = followPort.exists(query.followerId(), query.targetUserId());
        return new CheckFollowStatusResult(following);
    }
}
