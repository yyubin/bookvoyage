package org.yyubin.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

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

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "taste_tag", length = 100)
    private String tasteTag;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.id() != null ? user.id().value() : null)
                .email(user.email())
                .username(user.username())
                .password(user.password())
                .nickname(user.nickname())
                .bio(user.bio())
                .tasteTag(user.tasteTag())
                .profilePictureUrl(user.ProfileImageUrl())
                .role(user.role())
                .provider(user.provider())
                .createdAt(user.createdAt())
                .build();
    }

    public User toDomain() {
        return new User(
                new UserId(id),
                email,
                username,
                password,
                nickname,
                bio,
                tasteTag,
                role,
                provider,
                profilePictureUrl != null ? profilePictureUrl : "",
                createdAt
        );
    }
}
