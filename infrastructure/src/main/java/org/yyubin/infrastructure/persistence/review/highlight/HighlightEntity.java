package org.yyubin.infrastructure.persistence.review.highlight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.review.Highlight;
import org.yyubin.domain.review.HighlightId;

@Entity
@Table(name = "highlight")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HighlightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_value", nullable = false, length = 100)
    private String rawValue;

    @Column(name = "normalized_value", nullable = false, length = 100, unique = true)
    private String normalizedValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static HighlightEntity fromDomain(Highlight highlight) {
        return HighlightEntity.builder()
                .id(highlight.getId() != null ? highlight.getId().value() : null)
                .rawValue(highlight.getRawValue())
                .normalizedValue(highlight.getNormalizedValue())
                .createdAt(highlight.getCreatedAt())
                .build();
    }

    public Highlight toDomain() {
        return Highlight.of(
                new HighlightId(this.id),
                this.rawValue,
                this.normalizedValue,
                this.createdAt
        );
    }
}
