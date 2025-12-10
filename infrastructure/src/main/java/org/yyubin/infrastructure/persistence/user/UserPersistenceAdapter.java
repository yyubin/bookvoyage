package org.yyubin.infrastructure.persistence.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.auth.port.SaveUserPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.user.port.UpdateUserPort;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements
        LoadUserPort,
        SaveUserPort,
        UpdateUserPort,
        org.yyubin.application.auth.port.LoadUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User loadById(UserId userId) {
        return userJpaRepository.findById(userId.value())
                .map(UserEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId.value()));
    }

    @Override
    public User loadByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    @Override
    public Optional<User> loadByNickname(String nickname) {
        return userJpaRepository.findByNickname(nickname)
                .map(UserEntity::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = userJpaRepository.save(UserEntity.fromDomain(user));
        return entity.toDomain();
    }

    @Override
    @Transactional
    public User update(User user) {
        UserEntity entity = userJpaRepository.save(UserEntity.fromDomain(user));
        return entity.toDomain();
    }
}
