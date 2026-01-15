package org.yyubin.infrastructure.persistence.book;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookJpaRepository extends JpaRepository<BookEntity, Long> {

    Optional<BookEntity> findByIsbn10(String isbn10);

    Optional<BookEntity> findByIsbn13(String isbn13);

    Optional<BookEntity> findByGoogleVolumeId(String googleVolumeId);

    Slice<BookEntity> findByUpdatedAtAfterOrderByIdAsc(LocalDateTime updatedAt, Pageable pageable);
}
