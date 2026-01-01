package org.yyubin.recommendation.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * Elasticsearch Review Document
 * - 리뷰 텍스트 검색 및 분석용
 */
@Document(indexName = "review_content", createIndex = false)
@Setting(settingPath = "elasticsearch/review-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDocument {

    @Id
    private String id;

    // 작성자 ID
    @Field(type = FieldType.Long)
    private Long userId;

    // 리뷰 ID (cursor/정렬용)
    @Field(type = FieldType.Long)
    private Long reviewId;

    // 도서 ID
    @Field(type = FieldType.Long)
    private Long bookId;

    // 도서 제목
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String bookTitle;

    // 리뷰 요약
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String summary;

    // 리뷰 제목
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    // 리뷰 본문
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String content;

    // 하이라이트 문장
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private java.util.List<String> highlights;

    // 하이라이트 정규화
    @Field(type = FieldType.Keyword)
    private java.util.List<String> highlightsNorm;

    // 태그/키워드
    @Field(type = FieldType.Keyword)
    private java.util.List<String> keywords;

    // 평점
    @Field(type = FieldType.Float)
    private Float rating;

    // 장르
    @Field(type = FieldType.Keyword)
    private String genre;

    // 공개 여부
    @Field(type = FieldType.Keyword)
    private String visibility;

    // 작성일
    @Field(type = FieldType.Date,
           format = DateFormat.date_hour_minute_second_millis,
           pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    // 좋아요 수 (인기도 지표)
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    // 댓글 수
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    // 북마크 수
    @Field(type = FieldType.Integer)
    private Integer bookmarkCount;

    // 조회 수
    @Field(type = FieldType.Long)
    private Long viewCount;

    // 품질/잔류시간 보정 점수 (0.0~1.0 정규화)
    @Field(type = FieldType.Float)
    private Float dwellScore;

    // 평균 체류 시간(ms)
    @Field(type = FieldType.Long)
    private Long avgDwellMs;

    // 클릭률(0.0~1.0) - 인덱싱 시 저장, 정규화 필요
    @Field(type = FieldType.Float)
    private Float ctr;

    // 도달률(0.0~1.0) - 섹션 노출 대비 도달
    @Field(type = FieldType.Float)
    private Float reachRate;

    // 검색 강화용 합성 텍스트 (content 기반)
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String searchableText;

    public static String buildSearchableText(String content) {
        return content != null ? content : "";
    }
}
