package org.yyubin.domain.book;

/**
 * 책의 유형
 */
public enum BookType {
    /**
     * 출판된 도서 (ISBN 있음)
     */
    PUBLISHED_BOOK,

    /**
     * 웹소설 (플랫폼 기반, ISBN 없음)
     */
    WEB_NOVEL,

    /**
     * 기타 (자기계발서, 전자책 등)
     */
    OTHER;

    public static BookType getDefault() {
        return PUBLISHED_BOOK;
    }
}
