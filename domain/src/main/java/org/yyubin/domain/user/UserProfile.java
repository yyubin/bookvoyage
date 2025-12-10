package org.yyubin.domain.user;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Value Object for User profile information
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfile {
    private final String username;
    private final String bio;

    public static UserProfile of(String username, String bio) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        return new UserProfile(username, bio != null ? bio : "");
    }

    public UserProfile updateBio(String newBio) {
        return new UserProfile(this.username, newBio);
    }
}
