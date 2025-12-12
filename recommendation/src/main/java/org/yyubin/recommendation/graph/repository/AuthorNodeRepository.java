package org.yyubin.recommendation.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.graph.node.AuthorNode;

/**
 * AuthorNode Repository
 */
@Repository
public interface AuthorNodeRepository extends Neo4jRepository<AuthorNode, Long> {
}
