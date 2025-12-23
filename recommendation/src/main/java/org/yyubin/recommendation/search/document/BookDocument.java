package org.yyubin.recommendation.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;
import java.util.List;

/**
 * Elasticsearch Book Document
 * - 텍스트 기반 검색 및 콘텐츠 필터링용
 * - nori 분석기를 사용한 한국어 형태소 분석
 */
@Document(indexName = "books", createIndex = false)
@Setting(settingPath = "elasticsearch/book-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDocument {

    @Id
    private String id;  // Long을 String으로 변환하여 저장

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String isbn;

    // 저자 목록 (키워드 검색용)
    @Field(type = FieldType.Keyword)
    private List<String> authors;

    // 장르 목록
    @Field(type = FieldType.Keyword)
    private List<String> genres;

    // 주제/토픽 목록
    @Field(type = FieldType.Keyword)
    private List<String> topics;

    // 출판일
    @Field(type = FieldType.Date)
    private LocalDate publishedDate;

    // 인기도 지표 (랭킹용)
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer wishlistCount;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    // 평균 평점
    @Field(type = FieldType.Float)
    private Float averageRating;

    // 검색 스코어 부스트용 복합 필드
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String searchableText;  // title + description + authors 조합

    // 향후 Semantic Search용 벡터 필드 (Optional)
    // @Field(type = FieldType.Dense_Vector, dims = 768)
    // private float[] embedding;

    /**
     * 검색용 복합 텍스트 생성
     */
    public static String buildSearchableText(String title, String description, List<String> authors) {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append(title).append(" ");
        if (description != null) sb.append(description).append(" ");
        if (authors != null && !authors.isEmpty()) {
            sb.append(String.join(" ", authors));
        }
        return sb.toString().trim();
    }
}
