package org.yyubin.infrastructure.persistence.review;

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
import org.yyubin.domain.review.Keyword;
import org.yyubin.domain.review.KeywordId;

@Entity
@Table(name = "keyword")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_value", nullable = false, length = 100)
    private String rawValue;

    @Column(name = "normalized_value", nullable = false, length = 100, unique = true)
    private String normalizedValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static KeywordEntity fromDomain(Keyword keyword) {
        return KeywordEntity.builder()
                .id(keyword.getId() != null ? keyword.getId().value() : null)
                .rawValue(keyword.getRawValue())
                .normalizedValue(keyword.getNormalizedValue())
                .createdAt(keyword.getCreatedAt())
                .build();
    }

    public Keyword toDomain() {
        return Keyword.of(
                new KeywordId(this.id),
                this.rawValue,
                this.normalizedValue,
                this.createdAt
        );
    }
}
