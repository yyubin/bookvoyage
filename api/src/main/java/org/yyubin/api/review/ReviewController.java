package org.yyubin.api.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.review.dto.CreateReviewRequest;
import org.yyubin.api.review.dto.ReviewResponse;
import org.yyubin.api.review.dto.UserReviewPageResponse;
import org.yyubin.api.review.dto.UpdateReviewRequest;
import org.yyubin.application.review.CreateReviewUseCase;
import org.yyubin.application.review.DeleteReviewUseCase;
import org.yyubin.application.review.GetReviewUseCase;
import org.yyubin.application.review.GetUserReviewsUseCase;
import org.yyubin.application.review.UpdateReviewUseCase;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.command.CreateReviewCommand;
import org.yyubin.application.review.command.DeleteReviewCommand;
import org.yyubin.application.review.command.UpdateReviewCommand;
import org.yyubin.application.review.query.GetReviewQuery;
import org.yyubin.application.review.query.GetUserReviewsQuery;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewUseCase getReviewUseCase;
    private final GetUserReviewsUseCase getUserReviewsUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        ReviewResult result = getReviewUseCase.query(new GetReviewQuery(reviewId, viewerId));
        return ResponseEntity.ok(ReviewResponse.from(result));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserReviewPageResponse> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        PagedReviewResult result = getUserReviewsUseCase.query(new GetUserReviewsQuery(userId, viewerId, cursor, size));
        return ResponseEntity.ok(UserReviewPageResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        CreateReviewCommand command = new CreateReviewCommand(
                userId,
                request.title(),
                request.authors(),
                request.isbn10(),
                request.isbn13(),
                request.coverUrl(),
                request.publisher(),
                request.publishedDate(),
                request.description(),
                request.language(),
                request.pageCount(),
                request.googleVolumeId(),
                request.rating(),
                request.content(),
                parseVisibilityOrDefault(request.visibility()),
                parseGenreOrThrow(request.genre()),
                request.keywords()
        );

        ReviewResult result = createReviewUseCase.execute(command);
        return ResponseEntity.ok(ReviewResponse.from(result));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        UpdateReviewCommand command = new UpdateReviewCommand(
                reviewId,
                userId,
                request.title(),
                request.authors(),
                request.isbn10(),
                request.isbn13(),
                request.coverUrl(),
                request.publisher(),
                request.publishedDate(),
                request.description(),
                request.language(),
                request.pageCount(),
                request.googleVolumeId(),
                request.rating(),
                request.content(),
                parseVisibilityNullable(request.visibility()),
                parseGenreNullable(request.genre()),
                request.keywords()
        );

        ReviewResult result = updateReviewUseCase.execute(command);
        return ResponseEntity.ok(ReviewResponse.from(result));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        deleteReviewUseCase.execute(new DeleteReviewCommand(reviewId, userId));
        return ResponseEntity.noContent().build();
    }


    private ReviewVisibility parseVisibilityOrDefault(String visibility) {
        return ReviewVisibility.from(visibility);
    }

    private ReviewVisibility parseVisibilityNullable(String visibility) {
        if (visibility == null) {
            return null;
        }
        return ReviewVisibility.from(visibility);
    }

    private BookGenre parseGenreOrThrow(String genre) {
        BookGenre parsed = BookGenre.from(genre);
        if (parsed == null) {
            throw new IllegalArgumentException("Genre is required");
        }
        return parsed;
    }

    private BookGenre parseGenreNullable(String genre) {
        return BookGenre.from(genre);
    }

    private Long resolveViewerId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
