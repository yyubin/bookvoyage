package org.yyubin.api.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.profile.dto.CursorPageResponse;
import org.yyubin.api.profile.dto.FollowUserItemResponse;
import org.yyubin.api.profile.dto.ProfileSummaryResponse;
import org.yyubin.api.profile.dto.ReadingBookItemResponse;
import org.yyubin.api.profile.dto.ReviewItemResponse;
import org.yyubin.api.profile.dto.BookmarkedReviewItemResponse;
import java.util.List;
import org.yyubin.application.bookmark.GetBookmarksUseCase;
import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;
import org.yyubin.application.bookmark.query.GetBookmarksQuery;
import org.yyubin.application.profile.GetProfileSummaryUseCase;
import org.yyubin.application.profile.query.GetProfileSummaryQuery;
import org.yyubin.application.review.GetUserReviewsUseCase;
import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.query.GetUserReviewsQuery;
import org.yyubin.application.userbook.GetLatestReadingBooksUseCase;
import org.yyubin.application.userbook.dto.UserBookListResult;
import org.yyubin.application.userbook.query.GetLatestReadingBooksQuery;
import org.yyubin.application.user.GetFollowerUsersUseCase;
import org.yyubin.application.user.GetFollowingUsersUseCase;
import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.query.GetFollowerUsersQuery;
import org.yyubin.application.user.query.GetFollowingUsersQuery;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final GetProfileSummaryUseCase getProfileSummaryUseCase;
    private final GetFollowingUsersUseCase getFollowingUsersUseCase;
    private final GetFollowerUsersUseCase getFollowerUsersUseCase;
    private final GetUserReviewsUseCase getUserReviewsUseCase;
    private final GetLatestReadingBooksUseCase getLatestReadingBooksUseCase;
    private final GetBookmarksUseCase getBookmarksUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileSummaryResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ProfileSummaryResponse.from(getProfileSummaryUseCase.query(new GetProfileSummaryQuery(userId)))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileSummaryResponse> getMyProfile(
            @AuthenticationPrincipal Object principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(
                ProfileSummaryResponse.from(getProfileSummaryUseCase.query(new GetProfileSummaryQuery(userId)))
        );
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<CursorPageResponse<FollowUserItemResponse>> following(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        FollowPageResult result = getFollowingUsersUseCase.getFollowing(new GetFollowingUsersQuery(userId, cursor, size));
        return ResponseEntity.ok(toFollowPage(result));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<CursorPageResponse<FollowUserItemResponse>> followers(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        FollowPageResult result = getFollowerUsersUseCase.getFollowers(new GetFollowerUsersQuery(userId, cursor, size));
        return ResponseEntity.ok(toFollowPage(result));
    }

    @GetMapping("/{userId}/reviews")
    public ResponseEntity<CursorPageResponse<ReviewItemResponse>> reviews(
            @PathVariable Long userId,
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long viewerId = principal != null ? resolveUserId(principal) : null;
        PagedReviewResult result = getUserReviewsUseCase.query(new GetUserReviewsQuery(userId, viewerId, cursor, size));
        return ResponseEntity.ok(
                new CursorPageResponse<>(
                        result.reviews().stream().map(ReviewItemResponse::from).toList(),
                        result.nextCursor()
                )
        );
    }

    @GetMapping("/{userId}/reading-books")
    public ResponseEntity<List<ReadingBookItemResponse>> latestReadingBooks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int size
    ) {
        UserBookListResult result = getLatestReadingBooksUseCase.query(new GetLatestReadingBooksQuery(userId, size));
        return ResponseEntity.ok(result.items().stream().map(ReadingBookItemResponse::from).toList());
    }

    @GetMapping("/me/reading-books")
    public ResponseEntity<List<ReadingBookItemResponse>> myLatestReadingBooks(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "3") int size
    ) {
        Long userId = resolveUserId(principal);
        UserBookListResult result = getLatestReadingBooksUseCase.query(new GetLatestReadingBooksQuery(userId, size));
        return ResponseEntity.ok(result.items().stream().map(ReadingBookItemResponse::from).toList());
    }

    @GetMapping("/me/bookmarks")
    public ResponseEntity<List<BookmarkedReviewItemResponse>> latestBookmarkedReviews(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "3") int size
    ) {
        Long userId = resolveUserId(principal);
        ReviewBookmarkPageResult result = getBookmarksUseCase.query(new GetBookmarksQuery(userId, null, size));
        return ResponseEntity.ok(result.items().stream().map(BookmarkedReviewItemResponse::from).toList());
    }

    private CursorPageResponse<FollowUserItemResponse> toFollowPage(FollowPageResult result) {
        return new CursorPageResponse<>(
                result.users().stream().map(FollowUserItemResponse::from).toList(),
                result.nextCursor()
        );
    }

    private Long resolveUserId(Object principal) {
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        throw new IllegalArgumentException("Unauthorized");
    }
}
