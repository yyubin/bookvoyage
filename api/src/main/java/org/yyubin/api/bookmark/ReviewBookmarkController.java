package org.yyubin.api.bookmark;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.bookmark.dto.BookmarkActionResponse;
import org.yyubin.api.bookmark.dto.BookmarkPageResponse;
import org.yyubin.application.bookmark.AddBookmarkUseCase;
import org.yyubin.application.bookmark.GetBookmarksUseCase;
import org.yyubin.application.bookmark.RemoveBookmarkUseCase;
import org.yyubin.application.bookmark.command.AddBookmarkCommand;
import org.yyubin.application.bookmark.command.RemoveBookmarkCommand;
import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;
import org.yyubin.application.bookmark.query.GetBookmarksQuery;
import org.yyubin.domain.bookmark.ReviewBookmark;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewBookmarkController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final AddBookmarkUseCase addBookmarkUseCase;
    private final RemoveBookmarkUseCase removeBookmarkUseCase;
    private final GetBookmarksUseCase getBookmarksUseCase;

    @PostMapping("/reviews/{reviewId}/bookmark")
    public ResponseEntity<BookmarkActionResponse> addBookmark(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long reviewId
    ) {
        Long userId = resolveUserId(principal);
        ReviewBookmark bookmark = addBookmarkUseCase.add(new AddBookmarkCommand(userId, reviewId));
        return ResponseEntity.status(201).body(BookmarkActionResponse.bookmarked(reviewId, bookmark.createdAt()));
    }

    @DeleteMapping("/reviews/{reviewId}/bookmark")
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long reviewId
    ) {
        Long userId = resolveUserId(principal);
        removeBookmarkUseCase.remove(new RemoveBookmarkCommand(userId, reviewId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<BookmarkPageResponse> getBookmarks(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", required = false) @Min(1) @Max(MAX_PAGE_SIZE) Integer size
    ) {
        Long userId = resolveUserId(principal);
        int pageSize = size == null ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        ReviewBookmarkPageResult result = getBookmarksUseCase.query(new GetBookmarksQuery(userId, cursor, pageSize));
        return ResponseEntity.ok(BookmarkPageResponse.from(result));
    }

    private Long resolveUserId(Object principal) {
        if (principal instanceof org.yyubin.infrastructure.security.oauth2.CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        throw new IllegalArgumentException("Unauthorized");
    }
}
