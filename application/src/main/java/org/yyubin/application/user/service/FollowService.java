package org.yyubin.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.user.ToggleFollowUseCase;
import org.yyubin.application.user.command.ToggleFollowCommand;
import org.yyubin.application.user.dto.ToggleFollowResult;
import org.yyubin.application.user.port.FollowPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class FollowService implements ToggleFollowUseCase {

    private final FollowPort followPort;
    private final LoadUserPort loadUserPort;

    @Override
    @Transactional
    public ToggleFollowResult execute(ToggleFollowCommand command) {
        UserId followerId = new UserId(command.followerId());
        UserId followeeId = new UserId(command.targetUserId());

        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // ensure both users exist
        loadUserPort.loadById(followerId);
        loadUserPort.loadById(followeeId);

        boolean alreadyFollowing = followPort.exists(followerId.value(), followeeId.value());

        if (alreadyFollowing) {
            followPort.delete(followerId.value(), followeeId.value());
            return new ToggleFollowResult(false);
        } else {
            followPort.create(followerId.value(), followeeId.value());
            return new ToggleFollowResult(true);
        }
    }
}
