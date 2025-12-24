package org.yyubin.recommendation.review.graph;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewNode {

    @Id
    @Property("reviewId")
    private Long reviewId;

    @Property("userId")
    private Long userId;

    @Property("bookId")
    private Long bookId;

    @Relationship(type = "HAS_HIGHLIGHT")
    private Set<HighlightNode> highlights = new HashSet<>();
}
