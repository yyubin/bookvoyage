package org.yyubin.api.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.yyubin.domain.recommendation.UserAnalysis;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "사용자 독서 취향 분석 응답")
public record UserAnalysisResponse(

    @Schema(description = "사용자 ID", example = "123")
    Long userId,

    @Schema(description = "페르소나 타입", example = "fantasy_enthusiast")
    String personaType,

    @Schema(description = "분석 요약", example = "판타지 장르를 선호하며 모험과 성장 스토리를 좋아합니다")
    String summary,

    @Schema(description = "취향 키워드", example = "[\"모험\", \"마법\", \"성장\"]")
    List<String> keywords,

    @Schema(description = "AI 추천 도서 목록")
    List<BookRecommendationDto> recommendations,

    @Schema(description = "분석 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime analyzedAt
) {

    public static UserAnalysisResponse from(UserAnalysis analysis) {
        return new UserAnalysisResponse(
            analysis.userId(),
            analysis.personaType(),
            analysis.summary(),
            analysis.keywords(),
            analysis.recommendations().stream()
                .map(BookRecommendationDto::from)
                .toList(),
            analysis.analyzedAt()
        );
    }

    @Schema(description = "AI 추천 도서")
    public record BookRecommendationDto(

        @Schema(description = "책 제목", example = "해리포터와 마법사의 돌")
        String bookTitle,

        @Schema(description = "저자", example = "J.K. 롤링")
        String author,

        @Schema(description = "추천 이유", example = "마법과 모험이 가득한 성장 이야기")
        String reason
    ) {
        public static BookRecommendationDto from(UserAnalysis.BookRecommendation rec) {
            return new BookRecommendationDto(
                rec.bookTitle(),
                rec.author(),
                rec.reason()
            );
        }
    }
}
