package org.yyubin.domain.review;

public enum ReviewVisibility {
    PUBLIC,
    PRIVATE;

    public static ReviewVisibility from(String value) {
        if (value == null || value.isBlank()) {
            return PUBLIC;
        }
        try {
            return ReviewVisibility.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid review visibility: " + value);
        }
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
