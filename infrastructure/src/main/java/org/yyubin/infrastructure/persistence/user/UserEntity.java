package org.yyubin.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.user.UserProfile;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.getId() != null ? user.getId().getValue() : null)
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getProfile().getUsername())
                .bio(user.getProfile().getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toDomain() {
        return User.of(
                UserId.of(this.id),
                this.email,
                this.password,
                UserProfile.of(this.username, this.bio),
                this.createdAt
        );
    }
}
