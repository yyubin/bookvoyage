package org.yyubin.infrastructure.persistence.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.yyubin.domain.user.User;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserEntity> findByNickname(String nickname);

    Optional<UserEntity> findByUsername(String username);

    Slice<UserEntity> findByUpdatedAtAfterOrderByIdAsc(LocalDateTime updatedAt, Pageable pageable);
}
