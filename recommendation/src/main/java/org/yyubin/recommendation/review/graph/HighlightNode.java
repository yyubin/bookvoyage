package org.yyubin.recommendation.review.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Highlight")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HighlightNode {

    @Id
    @Property("normalizedValue")
    private String normalizedValue;

    @Property("rawValue")
    private String rawValue;
}
