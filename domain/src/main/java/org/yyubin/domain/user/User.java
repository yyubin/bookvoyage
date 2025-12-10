package org.yyubin.domain.user;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User Aggregate Root
 */
@Getter
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserId id;
    private final String email;
    private final String password;
    private final UserProfile profile;
    private final LocalDateTime createdAt;

    public static User of(UserId id, String email, String password, UserProfile profile, LocalDateTime createdAt) {
        validateEmail(email);
        validatePassword(password);
        Objects.requireNonNull(id, "User ID cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(profile, "Profile cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new User(id, email, password, profile, createdAt);
    }

    public static User create(String email, String password, String username, String bio) {
        validateEmail(email);
        validatePassword(password);

        return new User(
                null,
                email,
                password,
                UserProfile.of(username, bio),
                LocalDateTime.now()
        );
    }

    public User updateProfile(UserProfile newProfile) {
        return new User(this.id, this.email, this.password, newProfile, this.createdAt);
    }

    public User changePassword(String newPassword) {
        validatePassword(newPassword);
        return new User(this.id, this.email, newPassword, this.profile, this.createdAt);
    }

    private static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
