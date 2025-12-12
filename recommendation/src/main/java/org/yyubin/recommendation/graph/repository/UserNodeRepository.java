package org.yyubin.recommendation.graph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.graph.node.UserNode;

import java.util.List;

/**
 * UserNode Repository
 */
@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {

    /**
     * 명시적 bulk 저장 헬퍼
     */
    default <S extends UserNode> java.util.List<S> bulkSaveAll(Iterable<S> nodes) {
        java.util.List<S> saved = new java.util.ArrayList<>();
        this.saveAll(nodes).forEach(saved::add);
        return saved;
    }

    /**
     * 사용자가 조회한 도서들의 유사 도서 추천
     * - 2-hop 이웃 탐색
     */
    @Query("""
            MATCH (u:User {id: $userId})-[:VIEWED|WISHLISTED|LIKED_REVIEW_OF*1..2]-(book:Book)
            WHERE NOT (u)-[:VIEWED]->(book)
            RETURN book, COUNT(*) as score
            ORDER BY score DESC
            LIMIT $limit
            """)
    List<Object[]> findSimilarBooks(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 사용자가 선호하는 장르 기반 도서 추천
     */
    @Query("""
            MATCH (u:User {id: $userId})-[:VIEWED|WISHLISTED]->(b1:Book)-[:BELONGS_TO_GENRE]->(g:Genre)
            MATCH (b2:Book)-[:BELONGS_TO_GENRE]->(g)
            WHERE NOT (u)-[:VIEWED]->(b2) AND b1 <> b2
            RETURN b2, COUNT(DISTINCT g) as genreOverlap
            ORDER BY genreOverlap DESC
            LIMIT $limit
            """)
    List<Object[]> findBooksByPreferredGenres(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 사용자가 선호하는 저자 기반 도서 추천
     */
    @Query("""
            MATCH (u:User {id: $userId})-[:VIEWED|WISHLISTED]->(b1:Book)-[:WRITTEN_BY]->(a:Author)
            MATCH (b2:Book)-[:WRITTEN_BY]->(a)
            WHERE NOT (u)-[:VIEWED]->(b2) AND b1 <> b2
            RETURN b2, COUNT(DISTINCT a) as authorOverlap
            ORDER BY authorOverlap DESC
            LIMIT $limit
            """)
    List<Object[]> findBooksByPreferredAuthors(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 협업 필터링: 비슷한 취향의 사용자들이 본 도서 추천
     */
    @Query("""
            MATCH (u1:User {id: $userId})-[:VIEWED|WISHLISTED]->(b1:Book)<-[:VIEWED|WISHLISTED]-(u2:User)
            MATCH (u2)-[:VIEWED|WISHLISTED]->(b2:Book)
            WHERE NOT (u1)-[:VIEWED]->(b2) AND u1 <> u2
            RETURN b2, COUNT(DISTINCT u2) as similarUsers
            ORDER BY similarUsers DESC
            LIMIT $limit
            """)
    List<Object[]> findBooksByCollaborativeFiltering(@Param("userId") Long userId, @Param("limit") int limit);
}
