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
 * WISHLISTED 관계 속성
 * - User가 Book을 위시리스트에 추가한 이력
 */
@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistedRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private BookNode book;

    // 추가 시간
    private LocalDateTime addedAt;

    // 가중치 (위시리스트는 강한 관심 신호)
    @Builder.Default
    private Double weight = 0.3;
}
