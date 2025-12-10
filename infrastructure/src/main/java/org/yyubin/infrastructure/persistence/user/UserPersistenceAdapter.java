package org.yyubin.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.yyubin.application.auth.port.LoadUserPort;
import org.yyubin.application.auth.port.SaveUserPort;
import org.yyubin.domain.user.User;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User loadByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    @Override
    public User save(User user) {
        UserEntity entity = userJpaRepository.save(UserEntity.fromDomain(user));
        return entity.toDomain();
    }

}
