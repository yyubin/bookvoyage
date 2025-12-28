package org.yyubin.recommendation.review.search;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Builder
@Document(indexName = "review_content", createIndex = false)
@Setting(settingPath = "elasticsearch/review-settings.json")
@Mapping(mappingPath = "elasticsearch/review-mappings.json")
public class ReviewContentDocument {

    @Id
    private final Long reviewId;

    @Field(type = FieldType.Long)
    private final Long userId;

    @Field(type = FieldType.Keyword)
    private final String authorNickname;

    @Field(type = FieldType.Long)
    private final Long bookId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private final String bookTitle;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private final String summary;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private final String content;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private final List<String> highlights;

    @Field(type = FieldType.Keyword)
    private final List<String> highlightsNorm;

    @Field(type = FieldType.Keyword)
    private final List<String> keywords;

    @Field(type = FieldType.Keyword)
    private final String genre;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime createdAt;

    @Field(type = FieldType.Integer)
    private final Integer rating;
}
