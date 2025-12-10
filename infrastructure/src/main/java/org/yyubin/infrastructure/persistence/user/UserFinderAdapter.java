package org.yyubin.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.domain.review.UserFinder;

@Component
@RequiredArgsConstructor
public class UserFinderAdapter implements UserFinder {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Long findUserIdByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserEntity::getId)
                .orElse(null);
    }
}
