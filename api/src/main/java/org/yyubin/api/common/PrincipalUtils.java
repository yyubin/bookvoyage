package org.yyubin.api.common;

import org.springframework.security.core.userdetails.UserDetails;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

public final class PrincipalUtils {

    private PrincipalUtils() {
    }

    public static Long requireUserId(Object principal) {
        Long userId = resolveUserId(principal);
        if (userId == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return userId;
    }

    public static Long resolveUserId(Object principal) {
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
