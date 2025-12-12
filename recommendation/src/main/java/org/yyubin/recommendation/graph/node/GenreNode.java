package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * 장르 노드
 * - 도서의 장르 분류 (예: 철학, 문학, 과학 등)
 */
@Node("Genre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreNode {

    @Id
    private String name;  // 장르명을 ID로 사용 (예: "철학", "문학")

    private String description;

    // 인기도 지표
    private Integer bookCount;
}
