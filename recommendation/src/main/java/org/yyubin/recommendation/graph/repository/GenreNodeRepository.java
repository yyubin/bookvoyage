package org.yyubin.recommendation.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.graph.node.GenreNode;

/**
 * GenreNode Repository
 */
@Repository
public interface GenreNodeRepository extends Neo4jRepository<GenreNode, String> {
}
