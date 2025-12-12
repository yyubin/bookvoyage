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
 * VIEWED 관계 속성
 * - User가 Book을 조회한 이력
 */
@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewedRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private BookNode book;

    // 첫 조회 시간
    private LocalDateTime firstViewedAt;

    // 마지막 조회 시간
    private LocalDateTime lastViewedAt;

    // 조회 횟수
    @Builder.Default
    private Integer viewCount = 1;

    // 총 체류 시간 (초)
    @Builder.Default
    private Long totalDwellTimeSeconds = 0L;

    // 가중치 (추천 스코어 계산용)
    @Builder.Default
    private Double weight = 0.05;
}
