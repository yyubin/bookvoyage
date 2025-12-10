package org.yyubin.application.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.user.dto.FollowCountResult;
import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.dto.FollowUserView;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.user.query.GetFollowCountQuery;
import org.yyubin.application.user.query.GetFollowerUsersQuery;
import org.yyubin.application.user.query.GetFollowingUsersQuery;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowQueryService implements GetFollowingUsersUseCase, GetFollowerUsersUseCase, GetFollowCountUseCase {

    private final FollowQueryPort followQueryPort;
    private final LoadUserPort loadUserPort;

    @Override
    public FollowPageResult getFollowing(GetFollowingUsersQuery query) {
        List<Long> ids = followQueryPort.loadFollowingIds(query.userId(), query.cursor(), query.size() + 1);
        return toPageResult(ids, query.size());
    }

    @Override
    public FollowPageResult getFollowers(GetFollowerUsersQuery query) {
        List<Long> ids = followQueryPort.loadFollowerIds(query.userId(), query.cursor(), query.size() + 1);
        return toPageResult(ids, query.size());
    }

    @Override
    public FollowCountResult getCounts(GetFollowCountQuery query) {
        long following = followQueryPort.countFollowing(query.userId());
        long followers = followQueryPort.countFollowers(query.userId());
        return new FollowCountResult(following, followers);
    }

    private FollowPageResult toPageResult(List<Long> idsWithExtra, int size) {
        Long nextCursor = idsWithExtra.size() > size ? idsWithExtra.get(size) : null;
        List<Long> limitedIds = idsWithExtra.stream().limit(size).toList();
        List<FollowUserView> users = limitedIds.stream()
                .map(id -> loadUserPort.loadById(new UserId(id)))
                .map(this::toView)
                .toList();
        return new FollowPageResult(users, nextCursor);
    }

    private FollowUserView toView(User user) {
        return new FollowUserView(
                user.id().value(),
                user.username(),
                user.nickname(),
                user.ProfileImageUrl()
        );
    }
}
