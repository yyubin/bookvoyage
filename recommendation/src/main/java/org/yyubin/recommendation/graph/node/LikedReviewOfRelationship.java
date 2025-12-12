package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

/**
 * LIKED_REVIEW_OF 관계 속성
 * - User가 특정 Book의 리뷰에 좋아요를 누른 이력
 * - 간접적인 관심 신호
 */
@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikedReviewOfRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private BookNode book;

    // 리뷰 ID (참조용)
    private Long reviewId;

    // 좋아요 시간
    private LocalDateTime likedAt;

    // 좋아요 횟수 (같은 책의 여러 리뷰에 좋아요)
    @Builder.Default
    private Integer likeCount = 1;

    // 가중치
    @Builder.Default
    private Double weight = 0.15;
}
