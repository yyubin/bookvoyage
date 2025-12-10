package org.yyubin.domain.user;

import java.time.LocalDateTime;

public record User(
        UserId id,
        String email,
        String username,
        String password,
        String nickname,
        String bio,
        Role role,
        AuthProvider provider,
        String ProfileImageUrl,
        LocalDateTime createdAt
) {

    public User {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        // 기본값 설정 (provider를 먼저 설정해야 함)
        if (provider == null) provider = AuthProvider.LOCAL;
        if (bio == null) bio = "";
        if (role == null) role = Role.USER;
        if (createdAt == null) createdAt = LocalDateTime.now();

        // LOCAL provider인 경우에만 password 필수
        if (provider == AuthProvider.LOCAL) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password cannot be empty for LOCAL provider");
            }
        } else {
            // OAuth2 provider인 경우 password는 빈 문자열 허용
            if (password == null) password = "";
        }
    }

    public User updateProfile(String newUsername, String newBio, String newNickname, String newProfileImageUrl) {
        return new User(
                this.id,
                this.email,
                newUsername != null && !newUsername.isBlank() ? newUsername : this.username,
                this.password,
                newNickname != null ? newNickname : this.nickname,
                newBio != null ? newBio : this.bio,
                this.role,
                this.provider,
                newProfileImageUrl != null ? newProfileImageUrl : this.ProfileImageUrl,
                this.createdAt
        );
    }
}
