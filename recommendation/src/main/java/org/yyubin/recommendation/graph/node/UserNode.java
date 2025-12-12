package org.yyubin.recommendation.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 노드
 * - 추천 시스템의 주체
 */
@Node("User")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNode {

    @Id
    private Long id;

    private String username;
    private String email;
    private LocalDateTime createdAt;

    // 사용자가 조회한 도서들
    @Relationship(type = "VIEWED", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<ViewedRelationship> viewedBooks = new HashSet<>();

    // 사용자가 위시리스트에 추가한 도서들
    @Relationship(type = "WISHLISTED", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<WishlistedRelationship> wishlistedBooks = new HashSet<>();

    // 사용자가 좋아요한 리뷰의 도서들
    @Relationship(type = "LIKED_REVIEW_OF", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<LikedReviewOfRelationship> likedReviewBooks = new HashSet<>();
}
