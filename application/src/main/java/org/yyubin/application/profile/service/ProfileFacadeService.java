package org.yyubin.application.profile.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.bookmark.port.ReviewBookmarkRepository;
import org.yyubin.application.profile.GetProfileSummaryUseCase;
import org.yyubin.application.profile.dto.ProfileStatsResult;
import org.yyubin.application.profile.dto.ProfileSummaryResult;
import org.yyubin.application.profile.dto.ShelfStatsResult;
import org.yyubin.application.profile.query.GetProfileSummaryQuery;
import org.yyubin.application.review.port.ReviewCountPort;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.userbook.GetUserBookStatisticsUseCase;
import org.yyubin.application.userbook.dto.UserBookStatisticsResult;
import org.yyubin.application.userbook.query.GetUserBookStatisticsQuery;
import org.yyubin.application.wishlist.port.WishlistPort;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileFacadeService implements GetProfileSummaryUseCase {

    private final LoadUserPort loadUserPort;
    private final FollowQueryPort followQueryPort;
    private final ReviewCountPort reviewCountPort;
    private final ReviewBookmarkRepository reviewBookmarkRepository;
    private final WishlistPort wishlistPort;
    private final GetUserBookStatisticsUseCase getUserBookStatisticsUseCase;

    @Override
    public ProfileSummaryResult query(GetProfileSummaryQuery query) {
        UserId userId = new UserId(query.userId());
        User user = loadUserPort.loadById(userId);
        String name = resolveName(user);

        long followers = followQueryPort.countFollowers(userId.value());
        long following = followQueryPort.countFollowing(userId.value());
        long reviews = reviewCountPort.countByUserId(userId.value());

        UserBookStatisticsResult userBookStats = getUserBookStatisticsUseCase
                .query(new GetUserBookStatisticsQuery(userId.value()));
        long savedReviews = reviewBookmarkRepository.countByUser(userId);
        long bookmarks = wishlistPort.countByUser(userId);

        ProfileStatsResult stats = new ProfileStatsResult(reviews, followers, following);
        ShelfStatsResult shelves = new ShelfStatsResult(
                userBookStats.readingCount(),
                userBookStats.completedCount(),
                savedReviews,
                bookmarks
        );

        List<String> tags = Collections.emptyList();
        return new ProfileSummaryResult(
                user.id().value(),
                name,
                user.bio(),
                user.tasteTag(),
                user.ProfileImageUrl(),
                tags,
                stats,
                shelves
        );
    }

    private String resolveName(User user) {
        if (user.nickname() != null && !user.nickname().isBlank()) {
            return user.nickname();
        }
        return user.username();
    }
}
