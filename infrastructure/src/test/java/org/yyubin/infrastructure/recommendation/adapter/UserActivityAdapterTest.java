package org.yyubin.infrastructure.recommendation.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.yyubin.application.recommendation.port.out.UserActivityPort.ReviewActivity;
import org.yyubin.application.recommendation.port.out.UserActivityPort.ReviewWithKeywords;
import org.yyubin.application.recommendation.port.out.UserActivityPort.UserActivity;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkEntity;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeEntity;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserActivityAdapter 테스트")
class UserActivityAdapterTest {

    @Mock
    private ReviewBookmarkJpaRepository bookmarkRepository;

    @Mock
    private ReviewLikeJpaRepository likeRepository;

    @Mock
    private ReviewJpaRepository reviewRepository;

    @Mock
    private ReviewKeywordJpaRepository reviewKeywordRepository;

    @InjectMocks
    private UserActivityAdapter adapter;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("북마크와 좋아요 활동을 조회한다")
    void getUserActivity_RetrievesBookmarksAndLikes() {
        // Given
        Long userId = 1L;

        ReviewEntity bookmarkedReview = createReviewEntity(100L, 200L, BookGenre.FANTASY);
        ReviewBookmarkEntity bookmark = createBookmarkEntity(bookmarkedReview);
        when(bookmarkRepository.findByUserIdOrderByIdDesc(userId)).thenReturn(List.of(bookmark));

        ReviewLikeEntity like = new ReviewLikeEntity(1L, 101L, userId, now);
        when(likeRepository.findAll()).thenReturn(List.of(like));

        ReviewEntity likedReview = createReviewEntity(101L, 201L, BookGenre.ROMANCE);
        when(reviewRepository.findAllById(any())).thenReturn(List.of(bookmarkedReview, likedReview));

        when(reviewKeywordRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of());

        // When
        UserActivity result = adapter.getUserActivity(userId);

        // Then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.bookmarkedReviews()).hasSize(1);
        assertThat(result.bookmarkedReviews().get(0).reviewId()).isEqualTo(100L);
        assertThat(result.likedReviews()).hasSize(1);
        assertThat(result.likedReviews().get(0).reviewId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("북마크와 좋아요가 없으면 빈 리스트 반환")
    void getUserActivity_NoActivity_ReturnsEmptyLists() {
        // Given
        Long userId = 1L;
        when(bookmarkRepository.findByUserIdOrderByIdDesc(userId)).thenReturn(List.of());
        when(likeRepository.findAll()).thenReturn(List.of());
        when(reviewRepository.findAllById(any())).thenReturn(List.of());
        when(reviewKeywordRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of());

        // When
        UserActivity result = adapter.getUserActivity(userId);

        // Then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.bookmarkedReviews()).isEmpty();
        assertThat(result.likedReviews()).isEmpty();
    }

    @Test
    @DisplayName("북마크된 리뷰는 좋아요 리스트에서 제외한다")
    void getUserActivity_ExcludesBookmarkedFromLikes() {
        // Given
        Long userId = 1L;

        ReviewEntity bookmarkedReview = createReviewEntity(100L, 200L, BookGenre.FANTASY);
        ReviewBookmarkEntity bookmark = createBookmarkEntity(bookmarkedReview);
        when(bookmarkRepository.findByUserIdOrderByIdDesc(userId)).thenReturn(List.of(bookmark));

        // 같은 리뷰를 좋아요도 함
        ReviewLikeEntity likeOnBookmarked = new ReviewLikeEntity(1L, 100L, userId, now);
        ReviewLikeEntity likeOnOther = new ReviewLikeEntity(2L, 101L, userId, now);
        when(likeRepository.findAll()).thenReturn(List.of(likeOnBookmarked, likeOnOther));

        ReviewEntity otherReview = createReviewEntity(101L, 201L, BookGenre.ROMANCE);
        when(reviewRepository.findAllById(any())).thenReturn(List.of(bookmarkedReview, otherReview));

        when(reviewKeywordRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of());

        // When
        UserActivity result = adapter.getUserActivity(userId);

        // Then
        assertThat(result.bookmarkedReviews()).hasSize(1);
        assertThat(result.likedReviews()).hasSize(1);
        assertThat(result.likedReviews().get(0).reviewId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("최근 리뷰를 키워드와 함께 조회한다")
    void getRecentReviews_RetrievesWithKeywords() {
        // Given
        List<Long> userIds = List.of(1L, 2L);
        LocalDateTime since = now.minusDays(7);

        ReviewEntity review1 = createReviewEntity(100L, 200L, BookGenre.FANTASY);
        ReviewEntity review2 = createReviewEntity(101L, 201L, BookGenre.ROMANCE);
        when(reviewRepository.findByUserIdInAndCreatedAtAfter(userIds, since))
            .thenReturn(List.of(review1, review2));

        ReviewKeywordEntity keyword = createReviewKeywordEntity(100L, "magic");
        when(reviewKeywordRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of(keyword));

        ReviewLikeJpaRepository.ReviewLikeCount likeCount = mock(ReviewLikeJpaRepository.ReviewLikeCount.class);
        when(likeCount.getReviewId()).thenReturn(100L);
        when(likeCount.getCount()).thenReturn(5L);
        when(likeRepository.countByReviewIds(anyList())).thenReturn(List.of(likeCount));

        // When
        List<ReviewWithKeywords> result = adapter.getRecentReviews(userIds, since);

        // Then
        assertThat(result).hasSize(2);
        ReviewWithKeywords first = result.stream()
            .filter(r -> r.reviewId().equals(100L))
            .findFirst().orElseThrow();
        assertThat(first.keywords()).containsExactly("magic");
        assertThat(first.likeCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("최근 리뷰가 없으면 빈 리스트 반환")
    void getRecentReviews_NoReviews_ReturnsEmptyList() {
        // Given
        List<Long> userIds = List.of(1L, 2L);
        LocalDateTime since = now.minusDays(7);
        when(reviewRepository.findByUserIdInAndCreatedAtAfter(userIds, since))
            .thenReturn(List.of());

        // When
        List<ReviewWithKeywords> result = adapter.getRecentReviews(userIds, since);

        // Then
        assertThat(result).isEmpty();
    }

    private ReviewEntity createReviewEntity(Long id, Long bookId, BookGenre genre) {
        return ReviewEntity.builder()
            .id(id)
            .userId(1L)
            .bookId(bookId)
            .rating(4)
            .genre(genre)
            .createdAt(now)
            .build();
    }

    private ReviewBookmarkEntity createBookmarkEntity(ReviewEntity review) {
        return ReviewBookmarkEntity.builder()
            .id(1L)
            .review(review)
            .createdAt(now)
            .build();
    }

    private ReviewKeywordEntity createReviewKeywordEntity(Long reviewId, String keyword) {
        KeywordEntity keywordEntity = KeywordEntity.builder()
            .id(1L)
            .rawValue(keyword)
            .normalizedValue(keyword)
            .createdAt(now)
            .build();

        return ReviewKeywordEntity.builder()
            .id(new ReviewKeywordEntity.ReviewKeywordKey(reviewId, 1L))
            .keyword(keywordEntity)
            .build();
    }
}
