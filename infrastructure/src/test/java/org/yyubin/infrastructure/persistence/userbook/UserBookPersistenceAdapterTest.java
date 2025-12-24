package org.yyubin.infrastructure.persistence.userbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBookPersistenceAdapterTest {

    @Mock
    private UserBookJpaRepository repository;

    @InjectMocks
    private UserBookPersistenceAdapter adapter;

    private UserId userId;
    private BookId bookId;

    @BeforeEach
    void setUp() {
        userId = new UserId(1L);
        bookId = BookId.of(1L);
    }

    @Test
    @DisplayName("UserBook 저장")
    void save() {
        // given
        UserBook userBook = UserBook.create(userId, bookId, ReadingStatus.WANT_TO_READ);
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);
        UserBookEntity savedEntity = UserBookEntity.builder()
                .id(1L)
                .userId(entity.getUserId())
                .bookId(entity.getBookId())
                .status(entity.getStatus())
                .progressPercentage(entity.getProgressPercentage())
                .readingCount(entity.getReadingCount())
                .deleted(entity.getDeleted())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.save(any(UserBookEntity.class))).thenReturn(savedEntity);

        // when
        UserBook saved = adapter.save(userBook);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getBookId()).isEqualTo(bookId);
        verify(repository).save(any(UserBookEntity.class));
    }

    @Test
    @DisplayName("사용자와 책으로 UserBook 조회")
    void findByUserAndBook() {
        // given
        UserBookEntity entity = createUserBookEntity(1L, ReadingStatus.READING);
        when(repository.findByUserIdAndBookIdAndDeletedFalse(userId.value(), bookId.getValue()))
                .thenReturn(Optional.of(entity));

        // when
        Optional<UserBook> found = adapter.findByUserAndBook(userId, bookId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getBookId()).isEqualTo(bookId);
        verify(repository).findByUserIdAndBookIdAndDeletedFalse(userId.value(), bookId.getValue());
    }

    @Test
    @DisplayName("삭제된 UserBook은 조회되지 않음")
    void findByUserAndBook_deletedNotFound() {
        // given
        when(repository.findByUserIdAndBookIdAndDeletedFalse(userId.value(), bookId.getValue()))
                .thenReturn(Optional.empty());

        // when
        Optional<UserBook> found = adapter.findByUserAndBook(userId, bookId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("UserBook 존재 여부 확인")
    void exists() {
        // given
        when(repository.existsByUserIdAndBookIdAndDeletedFalse(userId.value(), bookId.getValue()))
                .thenReturn(true);

        // when
        boolean exists = adapter.exists(userId, bookId);

        // then
        assertThat(exists).isTrue();
        verify(repository).existsByUserIdAndBookIdAndDeletedFalse(userId.value(), bookId.getValue());
    }

    @Test
    @DisplayName("UserBook 소프트 삭제")
    void delete() {
        // given
        doNothing().when(repository).softDeleteByUserIdAndBookId(userId.value(), bookId.getValue());

        // when
        adapter.delete(userId, bookId);

        // then
        verify(repository).softDeleteByUserIdAndBookId(userId.value(), bookId.getValue());
    }

    @Test
    @DisplayName("사용자의 모든 UserBook 조회")
    void findByUser() {
        // given
        List<UserBookEntity> entities = List.of(
                createUserBookEntity(1L, ReadingStatus.WANT_TO_READ),
                createUserBookEntity(2L, ReadingStatus.READING)
        );
        when(repository.findByUserIdAndDeletedFalse(userId.value()))
                .thenReturn(entities);

        // when
        List<UserBook> userBooks = adapter.findByUser(userId);

        // then
        assertThat(userBooks).hasSize(2);
        verify(repository).findByUserIdAndDeletedFalse(userId.value());
    }

    @Test
    @DisplayName("사용자의 특정 상태 UserBook 조회")
    void findByUserAndStatus() {
        // given
        List<UserBookEntity> entities = List.of(
                createUserBookEntity(1L, ReadingStatus.READING),
                createUserBookEntity(2L, ReadingStatus.READING)
        );
        when(repository.findByUserIdAndStatusAndDeletedFalse(userId.value(), ReadingStatus.READING))
                .thenReturn(entities);

        // when
        List<UserBook> readingBooks = adapter.findByUserAndStatus(userId, ReadingStatus.READING);

        // then
        assertThat(readingBooks).hasSize(2);
        assertThat(readingBooks).allMatch(ub -> ub.getStatus() == ReadingStatus.READING);
        verify(repository).findByUserIdAndStatusAndDeletedFalse(userId.value(), ReadingStatus.READING);
    }

    @Test
    @DisplayName("책의 특정 상태 카운트 조회")
    void countByBookAndStatus() {
        // given
        when(repository.countByBookIdAndStatusAndDeletedFalse(bookId.getValue(), ReadingStatus.COMPLETED))
                .thenReturn(5L);

        // when
        long count = adapter.countByBookAndStatus(bookId, ReadingStatus.COMPLETED);

        // then
        assertThat(count).isEqualTo(5L);
        verify(repository).countByBookIdAndStatusAndDeletedFalse(bookId.getValue(), ReadingStatus.COMPLETED);
    }

    @Test
    @DisplayName("사용자의 특정 상태 카운트 조회")
    void countByUserAndStatus() {
        // given
        when(repository.countByUserIdAndStatusAndDeletedFalse(userId.value(), ReadingStatus.COMPLETED))
                .thenReturn(10L);

        // when
        long count = adapter.countByUserAndStatus(userId, ReadingStatus.COMPLETED);

        // then
        assertThat(count).isEqualTo(10L);
        verify(repository).countByUserIdAndStatusAndDeletedFalse(userId.value(), ReadingStatus.COMPLETED);
    }

    @Test
    @DisplayName("사용자의 전체 UserBook 카운트 조회")
    void countByUser() {
        // given
        when(repository.countByUserIdAndDeletedFalse(userId.value()))
                .thenReturn(15L);

        // when
        long count = adapter.countByUser(userId);

        // then
        assertThat(count).isEqualTo(15L);
        verify(repository).countByUserIdAndDeletedFalse(userId.value());
    }

    private UserBookEntity createUserBookEntity(Long id, ReadingStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return UserBookEntity.builder()
                .id(id)
                .userId(userId.value())
                .bookId(bookId.getValue())
                .status(status)
                .progressPercentage(status == ReadingStatus.COMPLETED ? 100 : 0)
                .readingCount(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
