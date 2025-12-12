package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 도서 노드
 * - 추천의 대상
 */
@Node("Book")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookNode {

    @Id
    private Long id;

    private String title;
    private String isbn;
    private String description;
    private LocalDate publishedDate;

    // 인기도 지표
    private Integer viewCount;
    private Integer wishlistCount;
    private Integer reviewCount;

    // 도서의 저자들
    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<AuthorNode> authors = new HashSet<>();

    // 도서의 장르들
    @Relationship(type = "BELONGS_TO_GENRE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<GenreNode> genres = new HashSet<>();

    // 도서의 주제/토픽들
    @Relationship(type = "HAS_TOPIC", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<TopicNode> topics = new HashSet<>();
}
