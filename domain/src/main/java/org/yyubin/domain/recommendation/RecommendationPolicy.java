package org.yyubin.domain.recommendation;

import org.yyubin.domain.user.UserId;

import java.util.List;

/**
 * Domain service interface for book recommendation policies
 * Implementations will be provided in the infrastructure layer
 */
public interface RecommendationPolicy {

    /**
     * Generates book recommendations for a specific user
     *
     * @param userId the user to generate recommendations for
     * @param limit maximum number of recommendations to return
     * @return list of recommendation results ordered by similarity score (descending)
     */
    List<RecommendationResult> recommendBooks(UserId userId, int limit);

    /**
     * Calculates similarity score between two users based on their reading preferences
     *
     * @param userId1 first user
     * @param userId2 second user
     * @return similarity score between the two users
     */
    SimilarityScore calculateUserSimilarity(UserId userId1, UserId userId2);
}
