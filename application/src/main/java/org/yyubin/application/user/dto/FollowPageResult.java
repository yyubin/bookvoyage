package org.yyubin.application.user.dto;

import java.util.List;

public record FollowPageResult(
        List<FollowUserView> users,
        Long nextCursor
) {
}
