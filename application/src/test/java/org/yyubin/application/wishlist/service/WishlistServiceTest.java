package org.yyubin.application.wishlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.wishlist.command.AddWishlistCommand;
import org.yyubin.application.wishlist.command.RemoveWishlistCommand;
import org.yyubin.application.wishlist.dto.WishlistResult;
import org.yyubin.application.wishlist.port.WishlistPort;
import org.yyubin.application.wishlist.query.GetWishlistQuery;
import org.yyubin.application.wishlist.query.WishlistSort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.wishlist.Wishlist;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService 테스트")
class WishlistServiceTest {

    @Mock
    private WishlistPort wishlistRepository;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private SaveBookPort saveBookPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private WishlistService wishlistService;

    private BookSearchItem bookSearchItem;
    private Book book;
    private UserId userId;

    @BeforeEach
    void setUp() {
        userId = new UserId(1L);

        bookSearchItem = BookSearchItem.of(
                "Test Book",
                Arrays.asList("Author 1"),
                "1234567890",
                "1234567890123",
                "http://cover.url",
                "Publisher",
                "2023-01-01",
                "Description",
                "en",
                300,
                "googleVolumeId123"
        );

        BookMetadata metadata = BookMetadata.of(
                "Test Book",
                Arrays.asList("Author 1"),
                "1234567890",
                "1234567890123",
                "http://cover.url",
                "Publisher",
                "2023-01-01",
                "Description",
                "en",
                300,
                "googleVolumeId123"
        );

        book = Book.of(BookId.of(1L), metadata);
    }

    @Test
    @DisplayName("위시리스트 추가 성공 - 새로운 책")
    void add_Success() {
        // Given
        AddWishlistCommand command = new AddWishlistCommand(1L, bookSearchItem);

        when(loadBookPort.loadByIdentifiers(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(saveBookPort.save(any(Book.class))).thenReturn(book);
        when(wishlistRepository.exists(userId, book.getId())).thenReturn(false);

        Wishlist savedWishlist = Wishlist.of(1L, userId, book.getId(), LocalDateTime.now());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(savedWishlist);

        // When
        wishlistService.add(command);

        // Then
        verify(loadBookPort).loadByIdentifiers("1234567890", "1234567890123", "googleVolumeId123");
        verify(saveBookPort).save(any(Book.class));
        verify(wishlistRepository).exists(userId, book.getId());
        verify(wishlistRepository).save(any(Wishlist.class));
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("위시리스트 추가 성공 - 기존 책")
    void add_SuccessWithExistingBook() {
        // Given
        AddWishlistCommand command = new AddWishlistCommand(1L, bookSearchItem);

        when(loadBookPort.loadByIdentifiers(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(book));
        when(wishlistRepository.exists(userId, book.getId())).thenReturn(false);

        Wishlist savedWishlist = Wishlist.of(1L, userId, book.getId(), LocalDateTime.now());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(savedWishlist);

        // When
        wishlistService.add(command);

        // Then
        verify(loadBookPort).loadByIdentifiers("1234567890", "1234567890123", "googleVolumeId123");
        verify(saveBookPort, never()).save(any());
        verify(wishlistRepository).exists(userId, book.getId());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("위시리스트 추가 - 이미 존재하는 경우 중복 추가하지 않음")
    void add_SkipIfAlreadyExists() {
        // Given
        AddWishlistCommand command = new AddWishlistCommand(1L, bookSearchItem);

        when(loadBookPort.loadByIdentifiers(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(book));
        when(wishlistRepository.exists(userId, book.getId())).thenReturn(true);

        // When
        wishlistService.add(command);

        // Then
        verify(wishlistRepository).exists(userId, book.getId());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("위시리스트 삭제 성공")
    void remove_Success() {
        // Given
        Long bookId = 1L;
        RemoveWishlistCommand command = new RemoveWishlistCommand(1L, bookId);

        // When
        wishlistService.remove(command);

        // Then
        verify(wishlistRepository).delete(userId, BookId.of(bookId));
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("위시리스트 조회 성공")
    void query_Success() {
        // Given
        GetWishlistQuery query = new GetWishlistQuery(1L, WishlistSort.RECENT);

        Wishlist wishlist1 = Wishlist.of(1L, userId, BookId.of(1L), LocalDateTime.now().minusDays(1));
        Wishlist wishlist2 = Wishlist.of(2L, userId, BookId.of(2L), LocalDateTime.now());

        when(wishlistRepository.findByUser(userId)).thenReturn(Arrays.asList(wishlist1, wishlist2));

        Book book1 = Book.of(BookId.of(1L), BookMetadata.of("Book 1", List.of("Author 1"), null, null, null, null, null, null, null, null, null));
        Book book2 = Book.of(BookId.of(2L), BookMetadata.of("Book 2", List.of("Author 2"), null, null, null, null, null, null, null, null, null));

        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(book1));
        when(loadBookPort.loadById(2L)).thenReturn(Optional.of(book2));

        // When
        WishlistResult result = wishlistService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(2);

        verify(wishlistRepository).findByUser(userId);
        verify(loadBookPort).loadById(1L);
        verify(loadBookPort).loadById(2L);
    }

    @Test
    @DisplayName("위시리스트 조회 - 빈 리스트")
    void query_EmptyList() {
        // Given
        GetWishlistQuery query = new GetWishlistQuery(1L, WishlistSort.RECENT);

        when(wishlistRepository.findByUser(userId)).thenReturn(List.of());

        // When
        WishlistResult result = wishlistService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();

        verify(wishlistRepository).findByUser(userId);
    }
}
