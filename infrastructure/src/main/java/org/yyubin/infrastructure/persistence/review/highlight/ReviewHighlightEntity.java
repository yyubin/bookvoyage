package org.yyubin.infrastructure.persistence.review.highlight;

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
import org.yyubin.domain.review.HighlightId;
import org.yyubin.domain.review.ReviewHighlight;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;

@Entity
@Table(
        name = "review_highlight",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_review_highlight_pair", columnNames = {"review_id", "highlight_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReviewHighlightEntity {

    @EmbeddedId
    private ReviewHighlightKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private ReviewEntity review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", insertable = false, updatable = false)
    private HighlightEntity highlight;

    public static ReviewHighlightEntity fromDomain(ReviewHighlight reviewHighlight) {
        return ReviewHighlightEntity.builder()
                .id(new ReviewHighlightKey(reviewHighlight.reviewId().getValue(), reviewHighlight.highlightId().value()))
                .build();
    }

    public ReviewHighlight toDomain() {
        return new ReviewHighlight(
                ReviewId.of(id.reviewId),
                new HighlightId(id.highlightId)
        );
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @lombok.EqualsAndHashCode
    public static class ReviewHighlightKey implements java.io.Serializable {
        @Column(name = "review_id")
        private Long reviewId;

        @Column(name = "highlight_id")
        private Long highlightId;
    }
}
