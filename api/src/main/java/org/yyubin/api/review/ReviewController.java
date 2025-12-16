package org.yyubin.api.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Reviews", description = "리뷰 관리 API - 리뷰 작성, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewUseCase getReviewUseCase;
    private final GetUserReviewsUseCase getUserReviewsUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;

    @Operation(
            summary = "리뷰 상세 조회",
            description = "리뷰 ID로 특정 리뷰의 상세 정보를 조회합니다. 공개 리뷰는 누구나 조회 가능하며, 비공개 리뷰는 작성자만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (비공개 리뷰)", content = @Content)
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        ReviewResult result = getReviewUseCase.query(new GetReviewQuery(reviewId, viewerId));
        return ResponseEntity.ok(ReviewResponse.from(result));
    }

    @Operation(
            summary = "사용자별 리뷰 목록 조회",
            description = "특정 사용자가 작성한 리뷰 목록을 커서 기반 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserReviewPageResponse> getUserReviews(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "커서 (다음 페이지 조회용, 이전 응답의 nextCursor 값)", example = "100")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        PagedReviewResult result = getUserReviewsUseCase.query(new GetUserReviewsQuery(userId, viewerId, cursor, size));
        return ResponseEntity.ok(UserReviewPageResponse.from(result));
    }

    @Operation(
            summary = "리뷰 작성",
            description = "새로운 리뷰를 작성합니다. 도서 정보와 함께 리뷰 내용, 평점, 공개 범위 등을 설정할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락, 유효성 검증 실패)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리뷰 작성 요청 데이터",
                    required = true
            )
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

    @Operation(
            summary = "리뷰 수정",
            description = "기존 리뷰를 수정합니다. 리뷰 작성자만 수정할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 리뷰)", content = @Content),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "리뷰 ID", required = true, example = "1")
            @PathVariable Long reviewId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리뷰 수정 요청 데이터",
                    required = true
            )
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

    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰를 삭제합니다 (Soft Delete). 리뷰 작성자만 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 리뷰)", content = @Content),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "리뷰 ID", required = true, example = "1")
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
