package org.yyubin.application.book.search.query;

import java.util.Arrays;

public enum PrintType {
    ALL("all"),
    BOOKS("books"),
    MAGAZINES("magazines");

    private final String externalValue;

    PrintType(String externalValue) {
        this.externalValue = externalValue;
    }

    public String externalValue() {
        return externalValue;
    }

    public static PrintType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value) || type.externalValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid print type: " + value));
    }
}
