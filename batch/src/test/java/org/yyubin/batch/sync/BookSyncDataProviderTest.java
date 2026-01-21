package org.yyubin.batch.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.wishlist.WishlistJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookSyncDataProvider 테스트")
class BookSyncDataProviderTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private WishlistJpaRepository wishlistJpaRepository;

    @Mock
    private ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    private BookSyncDataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new BookSyncDataProvider(reviewJpaRepository, wishlistJpaRepository, reviewKeywordJpaRepository);
    }

    @Test
    @DisplayName("BookSyncDto 정상 생성")
    void buildSyncData_Success() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test Book", "Test Author", "1234567890123", "A great book", "2024-01-15");

        ReviewEntity review1 = createMockReview(1L, 1L, 100L, BookGenre.FANTASY);
        ReviewEntity review2 = createMockReview(2L, 1L, 50L, BookGenre.ROMANCE);
        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of(review1, review2));
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(25L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(4.25);
        when(reviewKeywordJpaRepository.findByIdReviewIdIn(List.of(1L, 2L))).thenReturn(List.of());

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Book");
        assertThat(result.isbn()).isEqualTo("1234567890123");
        assertThat(result.description()).isEqualTo("A great book");
        assertThat(result.viewCount()).isEqualTo(150);
        assertThat(result.wishlistCount()).isEqualTo(25);
        assertThat(result.reviewCount()).isEqualTo(2);
        assertThat(result.averageRating()).isEqualTo(4.25f);
        assertThat(result.genres()).containsExactlyInAnyOrder("FANTASY", "ROMANCE");
    }

    @Test
    @DisplayName("리뷰가 없는 경우")
    void buildSyncData_NoReviews() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test Book", "Author", "1234567890123", "Description", "2024-01-01");
        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(10L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(null);

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.viewCount()).isEqualTo(0);
        assertThat(result.reviewCount()).isEqualTo(0);
        assertThat(result.averageRating()).isNull();
        assertThat(result.genres()).isEmpty();
    }

    @Test
    @DisplayName("ISBN10만 있는 경우")
    void buildSyncData_OnlyIsbn10() {
        // Given
        BookEntity bookEntity = mock(BookEntity.class);
        Book book = mock(Book.class);
        BookMetadata metadata = mock(BookMetadata.class);

        lenient().when(bookEntity.getId()).thenReturn(1L);
        when(bookEntity.toDomain()).thenReturn(book);
        when(book.getMetadata()).thenReturn(metadata);
        when(metadata.getTitle()).thenReturn("Test");
        when(metadata.getIsbn13()).thenReturn(null);
        when(metadata.getIsbn10()).thenReturn("1234567890");
        lenient().when(metadata.getDescription()).thenReturn("");
        lenient().when(metadata.getPublishedDate()).thenReturn(null);
        lenient().when(metadata.getAuthors()).thenReturn(List.of());

        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(0L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(null);

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.isbn()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("다양한 날짜 형식 파싱 - ISO 날짜")
    void buildSyncData_IsoDateFormat() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test", "Author", "123", "Desc", "2024-01-15");
        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(0L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(null);

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.publishedDate()).isNotNull();
        assertThat(result.publishedDate().getYear()).isEqualTo(2024);
        assertThat(result.publishedDate().getMonthValue()).isEqualTo(1);
        assertThat(result.publishedDate().getDayOfMonth()).isEqualTo(15);
    }

    @Test
    @DisplayName("잘못된 날짜 형식인 경우")
    void buildSyncData_InvalidDateFormat() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test", "Author", "123", "Desc", "invalid-date");
        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(0L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(null);

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.publishedDate()).isNull();
    }

    @Test
    @DisplayName("키워드(토픽) 조회")
    void buildSyncData_WithKeywords() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test", "Author", "123", "Desc", "2024-01-01");
        ReviewEntity review = createMockReview(1L, 1L, 10L, null);
        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of(review));
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(0L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(4.0);

        KeywordEntity keyword1 = mock(KeywordEntity.class);
        lenient().when(keyword1.getNormalizedValue()).thenReturn("magic");
        lenient().when(keyword1.getRawValue()).thenReturn("Magic");

        KeywordEntity keyword2 = mock(KeywordEntity.class);
        lenient().when(keyword2.getNormalizedValue()).thenReturn(null);
        lenient().when(keyword2.getRawValue()).thenReturn("Adventure");

        ReviewKeywordEntity rk1 = mock(ReviewKeywordEntity.class);
        ReviewKeywordEntity rk2 = mock(ReviewKeywordEntity.class);
        when(rk1.getKeyword()).thenReturn(keyword1);
        when(rk2.getKeyword()).thenReturn(keyword2);

        when(reviewKeywordJpaRepository.findByIdReviewIdIn(List.of(1L))).thenReturn(List.of(rk1, rk2));

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.topics()).containsExactlyInAnyOrder("magic", "Adventure");
    }

    @Test
    @DisplayName("viewCount null 처리")
    void buildSyncData_NullViewCount() {
        // Given
        BookEntity bookEntity = createMockBookEntity(1L, "Test", "Author", "123", "Desc", "2024-01-01");
        ReviewEntity review = mock(ReviewEntity.class);
        lenient().when(review.getId()).thenReturn(1L);
        when(review.getViewCount()).thenReturn(null);
        lenient().when(review.getGenre()).thenReturn(null);

        when(reviewJpaRepository.findByBookId(1L)).thenReturn(List.of(review));
        when(wishlistJpaRepository.countByBookId(1L)).thenReturn(0L);
        when(reviewJpaRepository.calculateAverageRating(1L)).thenReturn(null);
        when(reviewKeywordJpaRepository.findByIdReviewIdIn(anyList())).thenReturn(List.of());

        // When
        BookSyncDto result = provider.buildSyncData(bookEntity);

        // Then
        assertThat(result.viewCount()).isEqualTo(0);
    }

    private BookEntity createMockBookEntity(Long id, String title, String author, String isbn13, String description, String publishedDate) {
        BookEntity entity = mock(BookEntity.class);
        Book book = mock(Book.class);
        BookMetadata metadata = mock(BookMetadata.class);

        lenient().when(entity.getId()).thenReturn(id);
        when(entity.toDomain()).thenReturn(book);
        when(book.getMetadata()).thenReturn(metadata);
        when(metadata.getTitle()).thenReturn(title);
        when(metadata.getIsbn13()).thenReturn(isbn13);
        lenient().when(metadata.getIsbn10()).thenReturn(null);
        when(metadata.getDescription()).thenReturn(description);
        when(metadata.getPublishedDate()).thenReturn(publishedDate);
        when(metadata.getAuthors()).thenReturn(List.of(author));

        return entity;
    }

    private ReviewEntity createMockReview(Long id, Long bookId, Long viewCount, BookGenre genre) {
        ReviewEntity review = mock(ReviewEntity.class);
        when(review.getId()).thenReturn(id);
        lenient().when(review.getBookId()).thenReturn(bookId);
        when(review.getViewCount()).thenReturn(viewCount);
        when(review.getGenre()).thenReturn(genre);
        return review;
    }
}
