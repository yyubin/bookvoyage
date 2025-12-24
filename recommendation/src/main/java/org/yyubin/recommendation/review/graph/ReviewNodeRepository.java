package org.yyubin.recommendation.review.graph;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewNodeRepository extends Neo4jRepository<ReviewNode, Long> {

    @Query("""
            MATCH (h:Highlight {normalizedValue: $normalized})<-[:HAS_HIGHLIGHT]-(r:Review)
            WHERE $cursor IS NULL OR r.reviewId < $cursor
            RETURN r.reviewId AS reviewId
            ORDER BY r.reviewId DESC
            LIMIT $limit
            """)
    List<Long> findReviewIdsByHighlight(
            @Param("normalized") String normalized,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}
