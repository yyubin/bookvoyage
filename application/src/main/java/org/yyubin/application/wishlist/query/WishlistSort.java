package org.yyubin.application.wishlist.query;

import java.util.Arrays;

public enum WishlistSort {
    RECENT("recent"),
    TITLE("title"),
    AUTHOR("author"),
    PUBLISHED_DATE("publishedDate");

    private final String code;

    WishlistSort(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static WishlistSort from(String value) {
        if (value == null || value.isBlank()) {
            return RECENT;
        }
        return Arrays.stream(values())
                .filter(sort -> sort.name().equalsIgnoreCase(value) || sort.code.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid wishlist sort: " + value));
    }
}
