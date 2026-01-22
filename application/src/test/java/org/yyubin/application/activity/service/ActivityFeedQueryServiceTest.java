package org.yyubin.application.activity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.activity.dto.ActivityFeedPageResult;
import org.yyubin.application.activity.port.ActivityQueryPort;
import org.yyubin.application.activity.query.GetActivityFeedQuery;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.activity.ActivityItem;
import org.yyubin.domain.activity.ActivityType;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.Rating;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityFeedQueryService 테스트")
class ActivityFeedQueryServiceTest {

    @Mock
    private ActivityQueryPort activityQueryPort;

    @Mock
    private FollowQueryPort followQueryPort;

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private LoadKeywordsUseCase loadKeywordsUseCase;

    @Mock
    private LoadHighlightsUseCase loadHighlightsUseCase;

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private ActivityFeedQueryService activityFeedQueryService;

    private User testUser;
    private User testActor;
    private Book testBook;
    private Review testReview;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testUser = createTestUser(1L, "user@test.com", "testuser");
        testActor = createTestUser(2L, "actor@test.com", "testactor");
        testBook = createTestBook(1L);
        testReview = createTestReview(100L, 2L, 1L);
    }

    private User createTestUser(Long userId, String email, String username) {
        return new User(
                new UserId(userId),
                email,
                username,
                "password123",
                username + "_nick",
                "bio",
                "tasteTag",
                Role.USER,
                AuthProvider.LOCAL,
                "http://example.com/profile.jpg",
                LocalDateTime.now()
        );
    }

    private Book createTestBook(Long bookId) {
        Book book = Book.create(
                "Clean Code",
                List.of("Robert C. Martin"),
                "0132350882",
                "9780132350884",
                "http://example.com/cover.jpg",
                "Prentice Hall",
                "2008-08-01",
                "A Handbook of Agile Software Craftsmanship",
                "en",
                464,
                "google-volume-id-123"
        );
        return Book.of(BookId.of(bookId), book.getMetadata());
    }

    private Review createTestReview(Long reviewId, Long userId, Long bookId) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                ReviewVisibility.PUBLIC,
                false,
                0L,
                BookGenre.ESSAY,
                List.of()
        );
    }

    private Review createDeletedReview(Long reviewId, Long userId, Long bookId) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                ReviewVisibility.PUBLIC,
                true,
                0L,
                BookGenre.ESSAY,
                List.of()
        );
    }

    private Review createPrivateReview(Long reviewId, Long userId, Long bookId) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                ReviewVisibility.PRIVATE,
                false,
                0L,
                BookGenre.ESSAY,
                List.of()
        );
    }

    @Nested
    @DisplayName("query 메서드")
    class QueryMethod {

        @Test
        @DisplayName("피드 조회 성공 - 다음 페이지 있음")
        void query_Success_WithNextPage() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 2);
            List<Long> followingIds = List.of(2L, 3L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now.minusMinutes(1));
            ActivityItem activity2 = ActivityItem.reviewCreated(2L, new UserId(2L), ReviewId.of(101L), now.minusMinutes(2));
            ActivityItem activity3 = ActivityItem.reviewCreated(3L, new UserId(2L), ReviewId.of(102L), now.minusMinutes(3));

            Review review1 = createTestReview(100L, 2L, 1L);
            Review review2 = createTestReview(101L, 2L, 1L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(3)))
                    .thenReturn(List.of(activity1, activity2, activity3));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(review1);
            when(loadReviewPort.loadById(101L)).thenReturn(review2);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(2);
            assertThat(result.nextCursorEpochMillis()).isNotNull();

            verify(followQueryPort).loadFollowingIdsAll(1L);
            verify(activityQueryPort).loadActivities(eq(followingIds), eq(1L), any(), eq(3));
        }

        @Test
        @DisplayName("피드 조회 성공 - 다음 페이지 없음")
        void query_Success_NoNextPage() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(testReview);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursorEpochMillis()).isNull();
        }

        @Test
        @DisplayName("피드 조회 성공 - 빈 결과")
        void query_Success_EmptyResult() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of();

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
            assertThat(result.nextCursorEpochMillis()).isNull();
        }

        @Test
        @DisplayName("피드 조회 성공 - 커서 기반 페이지네이션")
        void query_Success_WithCursor() {
            // Given
            long cursorEpochMillis = now.toInstant(ZoneOffset.UTC).toEpochMilli();
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, cursorEpochMillis, 5);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now.minusMinutes(5));

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(LocalDateTime.class), eq(6)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(testReview);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursorEpochMillis()).isNull();
        }

        @Test
        @DisplayName("USER_FOLLOWED 타입 액티비티 - reviewId 없이 처리")
        void query_Success_UserFollowedActivity() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem followActivity = ActivityItem.userFollowed(1L, new UserId(2L), new UserId(3L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(followActivity));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).type()).isEqualTo(ActivityType.USER_FOLLOWED);
            assertThat(result.items().get(0).review()).isNull();
        }
    }

    @Nested
    @DisplayName("필터링 테스트")
    class FilteringTests {

        @Test
        @DisplayName("삭제된 리뷰가 있는 액티비티는 필터링됨")
        void query_FiltersDeletedReviews() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);
            Review deletedReview = createDeletedReview(100L, 2L, 1L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("비공개 리뷰가 있는 액티비티는 필터링됨")
        void query_FiltersPrivateReviews() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);
            Review privateReview = createPrivateReview(100L, 2L, 1L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(privateReview);

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 액티비티는 필터링됨")
        void query_FiltersWhenUserNotFound() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenThrow(new IllegalArgumentException("User not found"));

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("리뷰를 찾을 수 없는 액티비티는 필터링됨")
        void query_FiltersWhenReviewNotFound() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity1 = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity1));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenThrow(new IllegalArgumentException("Review not found"));

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("일부 유효한 액티비티만 반환")
        void query_ReturnsOnlyValidActivities() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L, 3L);

            User actor3 = createTestUser(3L, "actor3@test.com", "actor3");
            Review validReview = createTestReview(101L, 3L, 1L);
            Review deletedReview = createDeletedReview(100L, 2L, 1L);

            ActivityItem invalidActivity = ActivityItem.reviewCreated(1L, new UserId(2L), ReviewId.of(100L), now);
            ActivityItem validActivity = ActivityItem.reviewCreated(2L, new UserId(3L), ReviewId.of(101L), now.minusMinutes(1));

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(invalidActivity, validActivity));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadUserPort.loadById(new UserId(3L))).thenReturn(actor3);
            when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);
            when(loadReviewPort.loadById(101L)).thenReturn(validReview);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).activityId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("사이즈 처리 테스트")
    class SizeHandlingTests {

        @Test
        @DisplayName("사이즈가 null이면 기본값 20 사용")
        void query_UsesDefaultSizeWhenNull() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, null);
            List<Long> followingIds = List.of(2L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(21)))
                    .thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).isEmpty();
            verify(activityQueryPort).loadActivities(eq(followingIds), eq(1L), any(), eq(21));
        }

        @Test
        @DisplayName("사이즈가 MAX(50)보다 크면 50으로 제한")
        void query_LimitsSizeToMax() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 100);
            List<Long> followingIds = List.of(2L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(51)))
                    .thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            verify(activityQueryPort).loadActivities(eq(followingIds), eq(1L), any(), eq(51));
        }

        @Test
        @DisplayName("사이즈가 0이면 1로 설정")
        void query_SetsMinSizeToOne() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 0);
            List<Long> followingIds = List.of(2L);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(2)))
                    .thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            verify(activityQueryPort).loadActivities(eq(followingIds), eq(1L), any(), eq(2));
        }
    }

    @Nested
    @DisplayName("액티비티 타입별 테스트")
    class ActivityTypeTests {

        @Test
        @DisplayName("REVIEW_LIKED 타입 액티비티 처리")
        void query_Success_ReviewLikedActivity() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem likeActivity = ActivityItem.reviewLiked(1L, new UserId(2L), ReviewId.of(100L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(likeActivity));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(testReview);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).type()).isEqualTo(ActivityType.REVIEW_LIKED);
        }

        @Test
        @DisplayName("REVIEW_BOOKMARKED 타입 액티비티 처리")
        void query_Success_ReviewBookmarkedActivity() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem bookmarkActivity = ActivityItem.reviewBookmarked(1L, new UserId(2L), ReviewId.of(100L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(bookmarkActivity));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);
            when(loadReviewPort.loadById(100L)).thenReturn(testReview);
            when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
            when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
            when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).type()).isEqualTo(ActivityType.REVIEW_BOOKMARKED);
        }
    }

    @Nested
    @DisplayName("액터 정보 테스트")
    class ActorInfoTests {

        @Test
        @DisplayName("액터 정보가 올바르게 매핑됨")
        void query_MapsActorInfoCorrectly() {
            // Given
            GetActivityFeedQuery query = new GetActivityFeedQuery(1L, null, 10);
            List<Long> followingIds = List.of(2L);

            ActivityItem activity = ActivityItem.userFollowed(1L, new UserId(2L), new UserId(3L), now);

            when(followQueryPort.loadFollowingIdsAll(1L)).thenReturn(followingIds);
            when(activityQueryPort.loadActivities(eq(followingIds), eq(1L), any(), eq(11)))
                    .thenReturn(List.of(activity));
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(testActor);

            // When
            ActivityFeedPageResult result = activityFeedQueryService.query(query);

            // Then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).actor().userId()).isEqualTo(2L);
            assertThat(result.items().get(0).actor().username()).isEqualTo("testactor");
            assertThat(result.items().get(0).actor().nickname()).isEqualTo("testactor_nick");
            assertThat(result.items().get(0).actor().profileImageUrl()).isEqualTo("http://example.com/profile.jpg");
        }
    }
}
