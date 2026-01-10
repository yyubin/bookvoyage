package org.yyubin.infrastructure.persistence.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;

@Entity
@Table(
    name = "ai_user_analysis_recommendation",
    indexes = {
        @Index(name = "idx_ai_user_analysis_rec_analysis", columnList = "analysis_id"),
        @Index(name = "idx_ai_user_analysis_rec_rank", columnList = "analysis_id, recommendation_rank")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiUserAnalysisRecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analysis_id", nullable = false, insertable = false, updatable = false)
    private AiUserAnalysisEntity analysis;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_title", length = 255)
    private String bookTitle;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "recommendation_rank", nullable = false)
    private int rank;

    public AiUserAnalysisRecommendation toDomain() {
        return AiUserAnalysisRecommendation.of(
            id,
            analysisId,
            bookId,
            bookTitle,
            author,
            reason,
            rank
        );
    }

    public static AiUserAnalysisRecommendationEntity fromDomain(AiUserAnalysisRecommendation recommendation) {
        return AiUserAnalysisRecommendationEntity.builder()
            .id(recommendation.id())
            .analysisId(recommendation.analysisId())
            .bookId(recommendation.bookId())
            .bookTitle(recommendation.bookTitle())
            .author(recommendation.author())
            .reason(recommendation.reason())
            .rank(recommendation.rank())
            .build();
    }
}
