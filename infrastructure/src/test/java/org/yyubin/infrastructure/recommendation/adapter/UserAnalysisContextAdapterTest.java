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
import org.springframework.data.domain.PageRequest;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.ReviewSnapshot;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.UserAnalysisContext;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.UserBookSnapshot;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.search.SearchQueryLogEntity;
import org.yyubin.infrastructure.persistence.search.SearchQueryLogJpaRepository;
import org.yyubin.infrastructure.persistence.userbook.UserBookEntity;
import org.yyubin.infrastructure.persistence.userbook.UserBookJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserAnalysisContextAdapter 테스트")
class UserAnalysisContextAdapterTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private UserBookJpaRepository userBookJpaRepository;

    @Mock
    private SearchQueryLogJpaRepository searchQueryLogJpaRepository;

    @InjectMocks
    private UserAnalysisContextAdapter adapter;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("전체 컨텍스트를 로드한다")
    void loadContext_LoadsAllData() {
        // Given
        Long userId = 1L;
        int reviewLimit = 5;
        int libraryLimit = 5;
        int searchLimit = 5;
        LocalDateTime searchSince = now.minusDays(7);

        // Review setup
        ReviewEntity review = createReviewEntity(100L, 200L, userId);
        when(reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(review));

        BookEntity book = createBookEntity(200L, "Test Book", "[\"Author A\"]");
        when(bookJpaRepository.findAllById(anyList())).thenReturn(List.of(book));

        ReviewKeywordEntity keyword = createReviewKeywordEntity(100L, "fantasy");
        when(reviewKeywordJpaRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of(keyword));

        // UserBook setup
        UserBookEntity userBook = createUserBookEntity(200L, userId, ReadingStatus.READING);
        when(userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(userBook));

        // Search setup
        SearchQueryLogEntity searchLog = createSearchQueryLogEntity(userId, "fantasy books");
        when(searchQueryLogJpaRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), eq(searchSince), any(PageRequest.class)))
            .thenReturn(List.of(searchLog));

        // When
        UserAnalysisContext result = adapter.loadContext(userId, reviewLimit, libraryLimit, searchLimit, searchSince);

        // Then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.recentReviews()).hasSize(1);
        assertThat(result.recentLibraryUpdates()).hasSize(1);
        assertThat(result.recentSearchQueries()).containsExactly("fantasy books");
    }

    @Test
    @DisplayName("리뷰 스냅샷에 키워드가 포함된다")
    void loadContext_ReviewsIncludeKeywords() {
        // Given
        Long userId = 1L;

        ReviewEntity review = createReviewEntity(100L, 200L, userId);
        when(reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(review));

        BookEntity book = createBookEntity(200L, "Test Book", "[\"Author A\"]");
        when(bookJpaRepository.findAllById(anyList())).thenReturn(List.of(book));

        ReviewKeywordEntity keyword1 = createReviewKeywordEntity(100L, "magic");
        ReviewKeywordEntity keyword2 = createReviewKeywordEntity(100L, "adventure");
        when(reviewKeywordJpaRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of(keyword1, keyword2));

        when(userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());
        when(searchQueryLogJpaRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), any(), any(PageRequest.class)))
            .thenReturn(List.of());

        // When
        UserAnalysisContext result = adapter.loadContext(userId, 5, 5, 5, now.minusDays(7));

        // Then
        assertThat(result.recentReviews()).hasSize(1);
        ReviewSnapshot reviewSnapshot = result.recentReviews().get(0);
        assertThat(reviewSnapshot.keywords()).containsExactlyInAnyOrder("magic", "adventure");
    }

    @Test
    @DisplayName("limit이 0이면 빈 리스트 반환")
    void loadContext_ZeroLimit_ReturnsEmptyLists() {
        // Given
        Long userId = 1L;

        // When
        UserAnalysisContext result = adapter.loadContext(userId, 0, 0, 0, now.minusDays(7));

        // Then
        assertThat(result.recentReviews()).isEmpty();
        assertThat(result.recentLibraryUpdates()).isEmpty();
        assertThat(result.recentSearchQueries()).isEmpty();
    }

    @Test
    @DisplayName("searchSince가 null이면 검색 쿼리 빈 리스트 반환")
    void loadContext_NullSearchSince_ReturnsEmptySearchQueries() {
        // Given
        Long userId = 1L;

        when(reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());
        when(userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());

        // When
        UserAnalysisContext result = adapter.loadContext(userId, 5, 5, 5, null);

        // Then
        assertThat(result.recentSearchQueries()).isEmpty();
    }

    @Test
    @DisplayName("중복 검색 쿼리는 제거된다")
    void loadContext_DuplicateSearchQueries_AreRemoved() {
        // Given
        Long userId = 1L;
        LocalDateTime searchSince = now.minusDays(7);

        when(reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());
        when(userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());

        SearchQueryLogEntity search1 = createSearchQueryLogEntity(userId, "fantasy books");
        SearchQueryLogEntity search2 = createSearchQueryLogEntity(userId, "fantasy books"); // duplicate
        SearchQueryLogEntity search3 = createSearchQueryLogEntity(userId, "romance novels");
        when(searchQueryLogJpaRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), eq(searchSince), any(PageRequest.class)))
            .thenReturn(List.of(search1, search2, search3));

        // When
        UserAnalysisContext result = adapter.loadContext(userId, 5, 5, 5, searchSince);

        // Then
        assertThat(result.recentSearchQueries()).hasSize(2);
        assertThat(result.recentSearchQueries()).containsExactly("fantasy books", "romance novels");
    }

    @Test
    @DisplayName("라이브러리 업데이트 스냅샷을 로드한다")
    void loadContext_LoadsLibraryUpdates() {
        // Given
        Long userId = 1L;

        when(reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of());

        UserBookEntity userBook = createUserBookEntity(200L, userId, ReadingStatus.COMPLETED);
        when(userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(eq(userId), any(PageRequest.class)))
            .thenReturn(List.of(userBook));

        BookEntity book = createBookEntity(200L, "Completed Book", "[\"Author B\"]");
        when(bookJpaRepository.findAllById(anyList())).thenReturn(List.of(book));

        when(searchQueryLogJpaRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), any(), any(PageRequest.class)))
            .thenReturn(List.of());

        // When
        UserAnalysisContext result = adapter.loadContext(userId, 5, 5, 5, now.minusDays(7));

        // Then
        assertThat(result.recentLibraryUpdates()).hasSize(1);
        UserBookSnapshot snapshot = result.recentLibraryUpdates().get(0);
        assertThat(snapshot.bookId()).isEqualTo(200L);
        assertThat(snapshot.bookTitle()).isEqualTo("Completed Book");
        assertThat(snapshot.status()).isEqualTo("COMPLETED");
    }

    private ReviewEntity createReviewEntity(Long id, Long bookId, Long userId) {
        return ReviewEntity.builder()
            .id(id)
            .userId(userId)
            .bookId(bookId)
            .rating(4)
            .summary("Great book")
            .genre(BookGenre.FANTASY)
            .createdAt(now)
            .build();
    }

    private BookEntity createBookEntity(Long id, String title, String authors) {
        return BookEntity.builder()
            .id(id)
            .title(title)
            .authors(authors)
            .build();
    }

    private UserBookEntity createUserBookEntity(Long bookId, Long userId, ReadingStatus status) {
        return UserBookEntity.builder()
            .id(1L)
            .userId(userId)
            .bookId(bookId)
            .status(status)
            .progressPercentage(status == ReadingStatus.COMPLETED ? 100 : 50)
            .updatedAt(now)
            .build();
    }

    private SearchQueryLogEntity createSearchQueryLogEntity(Long userId, String query) {
        return SearchQueryLogEntity.builder()
            .id(1L)
            .userId(userId)
            .queryText(query)
            .normalizedQuery(query)
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
