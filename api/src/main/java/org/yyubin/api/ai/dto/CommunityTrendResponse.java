package org.yyubin.api.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.yyubin.domain.recommendation.CommunityTrend;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "커뮤니티 독서 트렌드 응답")
public record CommunityTrendResponse(

    @Schema(description = "트렌드 키워드", example = "[\"우울\", \"힐링\", \"성장\"]")
    List<String> keywords,

    @Schema(description = "트렌드 요약", example = "요즘 커뮤니티는 힐링과 위로를 주는 책을 선호합니다")
    String summary,

    @Schema(description = "장르별 트렌드")
    List<TrendingGenreDto> genres,

    @Schema(description = "분석 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime analyzedAt
) {

    public static CommunityTrendResponse from(CommunityTrend trend) {
        return new CommunityTrendResponse(
            trend.keywords(),
            trend.summary(),
            trend.genres().stream()
                .map(TrendingGenreDto::from)
                .toList(),
            trend.analyzedAt()
        );
    }

    @Schema(description = "트렌딩 장르")
    public record TrendingGenreDto(

        @Schema(description = "장르명", example = "판타지")
        String genre,

        @Schema(description = "전체 대비 비율 (0.0 ~ 1.0)", example = "0.355")
        Double percentage,

        @Schema(description = "트렌드 방향", example = "상승세", allowableValues = {"상승세", "하락세", "안정"})
        String mood
    ) {
        public static TrendingGenreDto from(CommunityTrend.TrendingGenre genre) {
            return new TrendingGenreDto(
                genre.genre(),
                genre.percentage(),
                genre.mood()
            );
        }
    }
}
