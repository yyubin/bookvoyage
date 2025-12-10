package org.yyubin.domain.review;

import java.util.Arrays;

public enum BookGenre {
    // 문학
    FICTION("문학 / 소설"),
    CLASSIC("고전문학"),
    POETRY("시"),
    ESSAY("에세이"),

    // 장르 문학
    FANTASY("판타지"),
    SCIENCE_FICTION("SF"),
    MYSTERY("미스터리"),
    THRILLER("스릴러"),
    HORROR("호러"),
    ROMANCE("로맨스"),
    HISTORICAL_FICTION("역사소설"),

    // 인문 / 사회
    PHILOSOPHY("철학"),
    PSYCHOLOGY("심리학"),
    SOCIOLOGY("사회학"),
    POLITICS("정치"),
    ECONOMICS("경제학"),
    HISTORY("역사"),

    // 자기계발 / 실용
    SELF_HELP("자기계발"),
    BUSINESS("비즈니스"),
    LEADERSHIP("리더십"),
    CAREER("커리어"),
    FINANCE("재테크"),

    // 과학 / 기술
    SCIENCE("과학"),
    TECHNOLOGY("기술 / 프로그래밍"),
    MATHEMATICS("수학"),
    MEDICINE("의학"),

    // 예술 / 문화
    ART("예술"),
    MUSIC("음악"),
    CULTURE("문화"),

    // 기타 비문학
    TRAVEL("여행"),
    COOKING("요리"),
    HEALTH("건강"),
    RELIGION("종교"),
    EDUCATION("교육"),
    CHILDREN("아동"),
    POCKET("단행본 / 기타");

    private final String displayName;

    BookGenre(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static BookGenre from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(BookGenre.values())
                .filter(genre -> genre.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid book genre: " + value));
    }
}
