package org.yyubin.domain.book;

public enum WebNovelPlatform {
    KAKAO_PAGE("카카오페이지", "https://page.kakao.com"),
    NAVER_SERIES("네이버 시리즈", "https://series.naver.com"),
    JOARA("조아라", "https://www.joara.com"),
    MUNPIA("문피아", "https://www.munpia.com"),
    RIDIBOOKS("리디북스", "https://ridibooks.com"),
    NOVELPIA("노벨피아", "https://novelpia.com"),
    OTHER("기타", null);

    private final String displayName;
    private final String baseUrl;

    WebNovelPlatform(String displayName, String baseUrl) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
