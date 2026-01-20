package org.yyubin.infrastructure.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.yyubin.domain.activity.ActivityItem;
import org.yyubin.domain.activity.ActivityType;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkEntity;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeEntity;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserFollowingEntity;
import org.yyubin.infrastructure.persistence.user.UserFollowingJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityQueryAdapter 테스트")
class ActivityQueryAdapterTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private ReviewLikeJpaRepository reviewLikeJpaRepository;

    @Mock
    private ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;

    @Mock
    private UserFollowingJpaRepository userFollowingJpaRepository;

    @InjectMocks
    private ActivityQueryAdapter adapter;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2024, 1, 15, 10, 0);
    }

    @Test
    @DisplayName("모든 타입의 활동을 조회하고 정렬한다")
    void loadActivities_LoadsAllTypesAndSorts() {
        // Given
        List<Long> followingIds = List.of(2L, 3L);
        Long userId = 1L;

        ReviewEntity review = createReviewEntity(100L, 2L, now.minusHours(1));
        when(reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), any(PageRequest.class)
        )).thenReturn(List.of(review));

        ReviewLikeEntity like = new ReviewLikeEntity(200L, 101L, 3L, now.minusHours(2));
        when(reviewLikeJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of(like));

        ReviewBookmarkEntity bookmark = createBookmarkEntity(300L, 2L, 102L, now.minusHours(3));
        when(reviewBookmarkJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of(bookmark));

        UserFollowingEntity follow = createFollowEntity(400L, 4L, userId, now);
        when(userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(follow));

        // When
        List<ActivityItem> result = adapter.loadActivities(followingIds, userId, null, 10);

        // Then
        assertThat(result).hasSize(4);
        // 최신순 정렬 확인 (follow가 가장 최근)
        assertThat(result.get(0).getType()).isEqualTo(ActivityType.USER_FOLLOWED);
        assertThat(result.get(1).getType()).isEqualTo(ActivityType.REVIEW_CREATED);
        assertThat(result.get(2).getType()).isEqualTo(ActivityType.REVIEW_LIKED);
        assertThat(result.get(3).getType()).isEqualTo(ActivityType.REVIEW_BOOKMARKED);
    }

    @Test
    @DisplayName("followingIds가 비어있으면 팔로우 활동만 조회한다")
    void loadActivities_EmptyFollowingIds_OnlyLoadsFollowActivities() {
        // Given
        Long userId = 1L;

        UserFollowingEntity follow = createFollowEntity(400L, 4L, userId, now);
        when(userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(follow));

        // When
        List<ActivityItem> result = adapter.loadActivities(List.of(), userId, null, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ActivityType.USER_FOLLOWED);
        verify(reviewJpaRepository, never()).findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(any(), any(), any());
    }

    @Test
    @DisplayName("followingIds가 null이면 팔로우 활동만 조회한다")
    void loadActivities_NullFollowingIds_OnlyLoadsFollowActivities() {
        // Given
        Long userId = 1L;

        UserFollowingEntity follow = createFollowEntity(400L, 4L, userId, now);
        when(userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(follow));

        // When
        List<ActivityItem> result = adapter.loadActivities(null, userId, null, 10);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("userId가 null이면 팔로우 활동을 조회하지 않는다")
    void loadActivities_NullUserId_NoFollowActivities() {
        // Given
        List<Long> followingIds = List.of(2L, 3L);

        when(reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), any(PageRequest.class)
        )).thenReturn(List.of());
        when(reviewLikeJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of());
        when(reviewBookmarkJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of());

        // When
        List<ActivityItem> result = adapter.loadActivities(followingIds, null, null, 10);

        // Then
        assertThat(result).isEmpty();
        verify(userFollowingJpaRepository, never()).findByFolloweeIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("cursor가 있으면 커서 기반 쿼리를 사용한다")
    void loadActivities_WithCursor_UsesCursorQuery() {
        // Given
        List<Long> followingIds = List.of(2L);
        Long userId = 1L;
        LocalDateTime cursor = now.minusHours(1);

        when(reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityAndCreatedAtBeforeOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), eq(cursor), any(PageRequest.class)
        )).thenReturn(List.of());
        when(reviewLikeJpaRepository.findByUserIdInAndCreatedAtBeforeOrderByCreatedAtDesc(
            eq(followingIds), eq(cursor), any(PageRequest.class)
        )).thenReturn(List.of());
        when(reviewBookmarkJpaRepository.findByUserIdInAndCreatedAtBeforeOrderByCreatedAtDesc(
            eq(followingIds), eq(cursor), any(PageRequest.class)
        )).thenReturn(List.of());
        when(userFollowingJpaRepository.findByFolloweeIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            eq(userId), eq(cursor), any(PageRequest.class)
        )).thenReturn(List.of());

        // When
        adapter.loadActivities(followingIds, userId, cursor, 10);

        // Then
        verify(reviewJpaRepository).findByUserIdInAndDeletedFalseAndVisibilityAndCreatedAtBeforeOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), eq(cursor), any(PageRequest.class)
        );
    }

    @Test
    @DisplayName("결과가 size보다 크면 잘라서 반환한다")
    void loadActivities_TrimResultsToSize() {
        // Given
        List<Long> followingIds = List.of(2L);
        Long userId = 1L;

        // 5개의 리뷰 활동 생성
        List<ReviewEntity> reviews = List.of(
            createReviewEntity(1L, 2L, now),
            createReviewEntity(2L, 2L, now.minusMinutes(1)),
            createReviewEntity(3L, 2L, now.minusMinutes(2)),
            createReviewEntity(4L, 2L, now.minusMinutes(3)),
            createReviewEntity(5L, 2L, now.minusMinutes(4))
        );
        when(reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), any(PageRequest.class)
        )).thenReturn(reviews);

        when(reviewLikeJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of());
        when(reviewBookmarkJpaRepository.findByUserIdInOrderByCreatedAtDesc(eq(followingIds), any(PageRequest.class)))
            .thenReturn(List.of());
        when(userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());

        // When
        List<ActivityItem> result = adapter.loadActivities(followingIds, userId, null, 3);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("리뷰 활동을 올바르게 변환한다")
    void loadActivities_ReviewActivity_ConvertedCorrectly() {
        // Given
        List<Long> followingIds = List.of(2L);
        ReviewEntity review = createReviewEntity(100L, 2L, now);
        when(reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
            eq(followingIds), eq(ReviewVisibility.PUBLIC), any(PageRequest.class)
        )).thenReturn(List.of(review));

        when(reviewLikeJpaRepository.findByUserIdInOrderByCreatedAtDesc(any(), any())).thenReturn(List.of());
        when(reviewBookmarkJpaRepository.findByUserIdInOrderByCreatedAtDesc(any(), any())).thenReturn(List.of());
        when(userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(any(), any())).thenReturn(List.of());

        // When
        List<ActivityItem> result = adapter.loadActivities(followingIds, null, null, 10);

        // Then
        assertThat(result).hasSize(1);
        ActivityItem activity = result.get(0);
        assertThat(activity.getId()).isEqualTo(100L);
        assertThat(activity.getType()).isEqualTo(ActivityType.REVIEW_CREATED);
        assertThat(activity.getActorId().value()).isEqualTo(2L);
        assertThat(activity.getReviewId().getValue()).isEqualTo(100L);
    }

    private ReviewEntity createReviewEntity(Long id, Long userId, LocalDateTime createdAt) {
        return ReviewEntity.builder()
            .id(id)
            .userId(userId)
            .bookId(1L)
            .rating(4)
            .visibility(ReviewVisibility.PUBLIC)
            .createdAt(createdAt)
            .build();
    }

    private ReviewBookmarkEntity createBookmarkEntity(Long id, Long userId, Long reviewId, LocalDateTime createdAt) {
        UserEntity user = UserEntity.builder().id(userId).build();
        ReviewEntity review = ReviewEntity.builder().id(reviewId).build();
        return ReviewBookmarkEntity.builder()
            .id(id)
            .user(user)
            .review(review)
            .createdAt(createdAt)
            .build();
    }

    private UserFollowingEntity createFollowEntity(Long id, Long followerId, Long followeeId, LocalDateTime createdAt) {
        return UserFollowingEntity.builder()
            .id(id)
            .followerId(followerId)
            .followeeId(followeeId)
            .createdAt(createdAt)
            .build();
    }
}
