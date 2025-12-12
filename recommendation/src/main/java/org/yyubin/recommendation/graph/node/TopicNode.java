package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * 주제/토픽 노드
 * - 도서의 세부 주제 분류 (예: 실존주의, 심리, 역사 등)
 */
@Node("Topic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicNode {

    @Id
    private String name;  // 토픽명을 ID로 사용 (예: "실존주의", "심리")

    private String description;

    // 인기도 지표
    private Integer bookCount;
}
