package org.yyubin.infrastructure.persistence.review;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.review.KeywordId;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewKeyword;

@Entity
@Table(
        name = "review_keyword",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_review_keyword_pair", columnNames = {"review_id", "keyword_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReviewKeywordEntity {

    @EmbeddedId
    private ReviewKeywordKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private ReviewEntity review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", insertable = false, updatable = false)
    private KeywordEntity keyword;

    public static ReviewKeywordEntity fromDomain(ReviewKeyword reviewKeyword) {
        return ReviewKeywordEntity.builder()
                .id(new ReviewKeywordKey(reviewKeyword.reviewId().getValue(), reviewKeyword.keywordId().value()))
                .build();
    }

    public ReviewKeyword toDomain() {
        return new ReviewKeyword(
                ReviewId.of(id.reviewId),
                new KeywordId(id.keywordId)
        );
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @lombok.EqualsAndHashCode
    public static class ReviewKeywordKey implements java.io.Serializable {
        @Column(name = "review_id")
        private Long reviewId;

        @Column(name = "keyword_id")
        private Long keywordId;
    }
}
