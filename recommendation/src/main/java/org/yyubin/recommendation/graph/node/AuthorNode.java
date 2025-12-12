package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * 저자 노드
 * - 도서와 연결되어 저자 기반 추천에 활용
 */
@Node("Author")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorNode {

    @Id
    private Long id;

    private String name;
    private String biography;

    // 인기도 지표
    private Integer bookCount;
}
