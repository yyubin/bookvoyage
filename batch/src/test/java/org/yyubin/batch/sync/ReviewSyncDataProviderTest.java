package org.yyubin.batch.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import org.yyubin.infrastructure.persistence.review.comment.ReviewCommentJpaRepository;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewSyncDataProvider 테스트")
class ReviewSyncDataProviderTest {

    @Mock
    private ReviewReactionJpaRepository reviewReactionJpaRepository;

    @Mock
    private ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;

    @Mock
    private ReviewCommentJpaRepository reviewCommentJpaRepository;

    @Mock
    private ReviewHighlightJpaRepository reviewHighlightJpaRepository;

    @Mock
    private HighlightJpaRepository highlightJpaRepository;

    @Mock
    private ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    @Mock
    private KeywordJpaRepository keywordJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    private ReviewSyncDataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ReviewSyncDataProvider(
                reviewReactionJpaRepository,
                reviewBookmarkJpaRepository,
                reviewCommentJpaRepository,
                reviewHighlightJpaRepository,
                highlightJpaRepository,
                reviewKeywordJpaRepository,
                keywordJpaRepository,
                bookJpaRepository
        );
    }

    @Test
    @DisplayName("ReviewSyncDto 정상 생성")
    void buildSyncData_Success() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 5, ReviewVisibility.PUBLIC, BookGenre.FANTASY, 500L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(25L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(10L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(5L);

        BookEntity book = mock(BookEntity.class);
        when(book.getTitle()).thenReturn("Test Book");
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.of(book));

        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.bookId()).isEqualTo(100L);
        assertThat(result.bookTitle()).isEqualTo("Test Book");
        assertThat(result.summary()).isEqualTo("Summary");
        assertThat(result.content()).isEqualTo("Content");
        assertThat(result.rating()).isEqualTo(5.0f);
        assertThat(result.visibility()).isEqualTo("PUBLIC");
        assertThat(result.genre()).isEqualTo("FANTASY");
        assertThat(result.likeCount()).isEqualTo(25);
        assertThat(result.bookmarkCount()).isEqualTo(10);
        assertThat(result.commentCount()).isEqualTo(5);
        assertThat(result.viewCount()).isEqualTo(500);
    }

    @Test
    @DisplayName("책을 찾을 수 없는 경우")
    void buildSyncData_BookNotFound() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.bookTitle()).isEmpty();
    }

    @Test
    @DisplayName("하이라이트 로드")
    void buildSyncData_WithHighlights() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        ReviewHighlightEntity rh = mock(ReviewHighlightEntity.class);
        ReviewHighlightEntity.ReviewHighlightKey rhKey = mock(ReviewHighlightEntity.ReviewHighlightKey.class);
        when(rh.getId()).thenReturn(rhKey);
        when(rhKey.getHighlightId()).thenReturn(1L);

        HighlightEntity highlight = mock(HighlightEntity.class);
        when(highlight.getId()).thenReturn(1L);
        when(highlight.getRawValue()).thenReturn("This is a highlight");
        when(highlight.getNormalizedValue()).thenReturn("this is a highlight");

        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of(rh));
        when(highlightJpaRepository.findByIdIn(List.of(1L))).thenReturn(List.of(highlight));

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.highlights()).containsExactly("This is a highlight");
        assertThat(result.highlightsNorm()).containsExactly("this is a highlight");
    }

    @Test
    @DisplayName("키워드 로드")
    void buildSyncData_WithKeywords() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        ReviewKeywordEntity rk = mock(ReviewKeywordEntity.class);
        ReviewKeywordEntity.ReviewKeywordKey rkKey = mock(ReviewKeywordEntity.ReviewKeywordKey.class);
        when(rk.getId()).thenReturn(rkKey);
        when(rkKey.getKeywordId()).thenReturn(1L);

        KeywordEntity keyword = mock(KeywordEntity.class);
        when(keyword.getId()).thenReturn(1L);
        when(keyword.getRawValue()).thenReturn("magic");

        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of(rk));
        when(keywordJpaRepository.findByIdIn(List.of(1L))).thenReturn(List.of(keyword));

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.keywords()).containsExactly("magic");
    }

    @Test
    @DisplayName("null 값들 처리")
    void buildSyncData_NullValues() {
        // Given
        ReviewEntity review = mock(ReviewEntity.class);
        when(review.getId()).thenReturn(1L);
        when(review.getUserId()).thenReturn(null);
        when(review.getBookId()).thenReturn(null);
        when(review.getSummary()).thenReturn(null);
        when(review.getContent()).thenReturn(null);
        when(review.getRating()).thenReturn(null);
        when(review.getVisibility()).thenReturn(null);
        when(review.getGenre()).thenReturn(null);
        when(review.getViewCount()).thenReturn(null);
        when(review.getCreatedAt()).thenReturn(null);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(null)).thenReturn(Optional.empty());
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.userId()).isNull();
        assertThat(result.bookId()).isNull();
        assertThat(result.summary()).isNull();
        assertThat(result.content()).isNull();
        assertThat(result.rating()).isNull();
        assertThat(result.visibility()).isNull();
        assertThat(result.genre()).isNull();
        assertThat(result.viewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("count 오버플로우 처리")
    void buildSyncData_CountOverflow() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(Long.MAX_VALUE);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.likeCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("하이라이트 없는 경우")
    void buildSyncData_NoHighlights() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.highlights()).isEmpty();
        assertThat(result.highlightsNorm()).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 하이라이트 필터링")
    void buildSyncData_BlankHighlightsFiltered() {
        // Given
        ReviewEntity review = createMockReview(1L, 10L, 100L, "Summary", "Content", 4, ReviewVisibility.PUBLIC, null, 0L);

        when(reviewReactionJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewBookmarkJpaRepository.countByReviewId(1L)).thenReturn(0L);
        when(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(1L)).thenReturn(0L);
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());

        ReviewHighlightEntity rh = mock(ReviewHighlightEntity.class);
        ReviewHighlightEntity.ReviewHighlightKey rhKey = mock(ReviewHighlightEntity.ReviewHighlightKey.class);
        when(rh.getId()).thenReturn(rhKey);
        when(rhKey.getHighlightId()).thenReturn(1L);

        HighlightEntity highlight = mock(HighlightEntity.class);
        when(highlight.getId()).thenReturn(1L);
        when(highlight.getRawValue()).thenReturn("   ");
        when(highlight.getNormalizedValue()).thenReturn("");

        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of(rh));
        when(highlightJpaRepository.findByIdIn(List.of(1L))).thenReturn(List.of(highlight));

        // When
        ReviewSyncDto result = provider.buildSyncData(review);

        // Then
        assertThat(result.highlights()).isEmpty();
        assertThat(result.highlightsNorm()).isEmpty();
    }

    private ReviewEntity createMockReview(Long id, Long userId, Long bookId, String summary, String content,
                                          Integer rating, ReviewVisibility visibility, BookGenre genre, Long viewCount) {
        ReviewEntity review = mock(ReviewEntity.class);
        when(review.getId()).thenReturn(id);
        when(review.getUserId()).thenReturn(userId);
        when(review.getBookId()).thenReturn(bookId);
        when(review.getSummary()).thenReturn(summary);
        when(review.getContent()).thenReturn(content);
        when(review.getRating()).thenReturn(rating);
        when(review.getVisibility()).thenReturn(visibility);
        when(review.getGenre()).thenReturn(genre);
        when(review.getViewCount()).thenReturn(viewCount);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 1, 1, 10, 0));
        return review;
    }
}
