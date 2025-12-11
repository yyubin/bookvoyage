package org.yyubin.application.book.search.query;

import java.util.Arrays;

public enum SearchOrder {
    RELEVANCE("relevance"),
    NEWEST("newest");

    private final String externalValue;

    SearchOrder(String externalValue) {
        this.externalValue = externalValue;
    }

    public String externalValue() {
        return externalValue;
    }

    public static SearchOrder from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(order -> order.name().equalsIgnoreCase(value) || order.externalValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid search order: " + value));
    }
}
