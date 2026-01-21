package org.yyubin.batch.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionEntity;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.wishlist.WishlistEntity;
import org.yyubin.infrastructure.persistence.wishlist.WishlistJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSyncDataProvider 테스트")
class UserSyncDataProviderTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private WishlistJpaRepository wishlistJpaRepository;

    @Mock
    private ReviewReactionJpaRepository reviewReactionJpaRepository;

    private UserSyncDataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new UserSyncDataProvider(reviewJpaRepository, wishlistJpaRepository, reviewReactionJpaRepository);
    }

    @Test
    @DisplayName("UserSyncDto 정상 생성")
    void buildSyncData_Success() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");

        ReviewEntity review1 = createMockReview(1L, 100L, LocalDateTime.of(2024, 1, 1, 10, 0), 50L);
        ReviewEntity review2 = createMockReview(2L, 100L, LocalDateTime.of(2024, 1, 5, 15, 0), 30L);
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of(review1, review2));

        WishlistEntity wishlist = createMockWishlist(200L, LocalDateTime.of(2024, 1, 3, 12, 0));
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of(wishlist));

        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.viewedBooks()).hasSize(1);
        assertThat(result.viewedBooks().get(0).bookId()).isEqualTo(100L);
        assertThat(result.viewedBooks().get(0).viewCount()).isEqualTo(80);
        assertThat(result.wishlistedBooks()).hasSize(1);
        assertThat(result.wishlistedBooks().get(0).bookId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("리뷰가 없는 경우")
    void buildSyncData_NoReviews() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.viewedBooks()).isEmpty();
        assertThat(result.wishlistedBooks()).isEmpty();
        assertThat(result.likedReviewBooks()).isEmpty();
    }

    @Test
    @DisplayName("동일 책에 여러 리뷰가 있는 경우 집계")
    void buildSyncData_MultipleReviewsSameBook() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");

        LocalDateTime earlier = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime later = LocalDateTime.of(2024, 1, 10, 15, 0);

        ReviewEntity review1 = createMockReview(1L, 100L, earlier, 50L);
        ReviewEntity review2 = createMockReview(2L, 100L, later, 30L);
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of(review1, review2));
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.viewedBooks()).hasSize(1);
        UserSyncDto.ViewedBook viewedBook = result.viewedBooks().get(0);
        assertThat(viewedBook.bookId()).isEqualTo(100L);
        assertThat(viewedBook.firstViewedAt()).isEqualTo(earlier);
        assertThat(viewedBook.lastViewedAt()).isEqualTo(later);
        assertThat(viewedBook.viewCount()).isEqualTo(80);
    }

    @Test
    @DisplayName("좋아요한 리뷰 조회")
    void buildSyncData_WithLikedReviews() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());

        ReviewReactionEntity reaction = mock(ReviewReactionEntity.class);
        when(reaction.getReviewId()).thenReturn(10L);
        when(reaction.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 1, 5, 10, 0));

        ReviewEntity likedReview = mock(ReviewEntity.class);
        when(likedReview.getId()).thenReturn(10L);
        when(likedReview.getBookId()).thenReturn(200L);

        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of(reaction));
        when(reviewJpaRepository.findAllById(List.of(10L))).thenReturn(List.of(likedReview));

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.likedReviewBooks()).hasSize(1);
        UserSyncDto.LikedReviewBook liked = result.likedReviewBooks().get(0);
        assertThat(liked.reviewId()).isEqualTo(10L);
        assertThat(liked.bookId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("bookId가 null인 리뷰는 무시")
    void buildSyncData_NullBookIdIgnored() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");

        ReviewEntity review = mock(ReviewEntity.class);
        when(review.getBookId()).thenReturn(null);
        lenient().when(review.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of(review));
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.viewedBooks()).isEmpty();
    }

    @Test
    @DisplayName("wishlist의 bookId가 null인 경우 필터링")
    void buildSyncData_NullWishlistBookId() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of());

        WishlistEntity wishlist = mock(WishlistEntity.class);
        BookEntity book = mock(BookEntity.class);
        when(wishlist.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(null);
        lenient().when(wishlist.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of(wishlist));
        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.wishlistedBooks()).isEmpty();
    }

    @Test
    @DisplayName("viewCount null 처리")
    void buildSyncData_NullViewCount() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");

        ReviewEntity review = mock(ReviewEntity.class);
        when(review.getBookId()).thenReturn(100L);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(review.getViewCount()).thenReturn(null);

        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of(review));
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.viewedBooks()).hasSize(1);
        assertThat(result.viewedBooks().get(0).viewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요한 리뷰의 bookId가 null인 경우 필터링")
    void buildSyncData_LikedReviewWithNullBookId() {
        // Given
        UserEntity user = createMockUser(1L, "testuser", "test@example.com");
        when(reviewJpaRepository.findByUserId(1L)).thenReturn(List.of());
        when(wishlistJpaRepository.findByUserId(1L)).thenReturn(List.of());

        ReviewReactionEntity reaction = mock(ReviewReactionEntity.class);
        when(reaction.getReviewId()).thenReturn(10L);
        lenient().when(reaction.getCreatedAt()).thenReturn(LocalDateTime.now());

        ReviewEntity likedReview = mock(ReviewEntity.class);
        when(likedReview.getId()).thenReturn(10L);
        when(likedReview.getBookId()).thenReturn(null);

        when(reviewReactionJpaRepository.findByUserId(1L)).thenReturn(List.of(reaction));
        when(reviewJpaRepository.findAllById(List.of(10L))).thenReturn(List.of(likedReview));

        // When
        UserSyncDto result = provider.buildSyncData(user);

        // Then
        assertThat(result.likedReviewBooks()).isEmpty();
    }

    private UserEntity createMockUser(Long id, String username, String email) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getEmail()).thenReturn(email);
        when(user.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        return user;
    }

    private ReviewEntity createMockReview(Long id, Long bookId, LocalDateTime createdAt, Long viewCount) {
        ReviewEntity review = mock(ReviewEntity.class);
        lenient().when(review.getId()).thenReturn(id);
        when(review.getBookId()).thenReturn(bookId);
        when(review.getCreatedAt()).thenReturn(createdAt);
        when(review.getViewCount()).thenReturn(viewCount);
        return review;
    }

    private WishlistEntity createMockWishlist(Long bookId, LocalDateTime createdAt) {
        WishlistEntity wishlist = mock(WishlistEntity.class);
        BookEntity book = mock(BookEntity.class);
        when(wishlist.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(bookId);
        when(wishlist.getCreatedAt()).thenReturn(createdAt);
        return wishlist;
    }
}
