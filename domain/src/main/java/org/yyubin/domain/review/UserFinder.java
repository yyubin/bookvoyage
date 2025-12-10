package org.yyubin.domain.review;

public interface UserFinder {
    /**
     * Resolve username to userId. Return null if not found.
     */
    Long findUserIdByUsername(String username);
}
