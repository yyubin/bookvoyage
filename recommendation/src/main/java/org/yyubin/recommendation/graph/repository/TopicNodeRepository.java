package org.yyubin.recommendation.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.graph.node.TopicNode;

/**
 * TopicNode Repository
 */
@Repository
public interface TopicNodeRepository extends Neo4jRepository<TopicNode, String> {
}
