package org.yyubin.infrastructure.persistence.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookJpaRepository extends JpaRepository<BookEntity, Long> {

    Optional<BookEntity> findByIsbn(String isbn);

    List<BookEntity> findByTitleContaining(String title);

    List<BookEntity> findByAuthorContaining(String author);

    boolean existsByIsbn(String isbn);
}
