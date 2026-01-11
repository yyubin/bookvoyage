package org.yyubin.application.recommendation.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.domain.recommendation.SimilarUser;
import org.yyubin.domain.recommendation.UserTasteVector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 유사 사용자 찾기 Use Case
 * 사용자 취향 벡터를 기반으로 유사한 취향의 사용자를 찾습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSimilarUsersUseCase {

    private final ReviewCircleCachePort cachePort;

    private static final int TOP_N_SIMILAR_USERS = 50;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.1;

    /**
     * 특정 사용자의 유사 사용자 찾기
     */
    public List<SimilarUser> execute(Long userId, List<UserTasteVector> allTasteVectors) {
        // 1. 대상 사용자의 취향 벡터 찾기
        UserTasteVector userVector = allTasteVectors.stream()
            .filter(v -> v.userId().equals(userId))
            .findFirst()
            .orElse(null);

        if (userVector == null || userVector.vector().isEmpty()) {
            log.debug("User {} has no taste vector - skipping", userId);
            return List.of();
        }

        // 2. 다른 모든 사용자와 유사도 계산
        List<SimilarUser> similarities = new ArrayList<>();

        for (UserTasteVector otherVector : allTasteVectors) {
            if (otherVector.userId().equals(userId)) {
                continue;
            }

            if (otherVector.vector().isEmpty()) {
                continue;
            }

            double similarity = calculateCosineSimilarity(
                userVector.vector(),
                otherVector.vector()
            );

            if (similarity >= MIN_SIMILARITY_THRESHOLD) {
                similarities.add(SimilarUser.of(otherVector.userId(), similarity));
            }
        }

        // 3. 상위 N명 선택
        List<SimilarUser> topSimilarUsers = similarities.stream()
            .sorted(Comparator.reverseOrder())
            .limit(TOP_N_SIMILAR_USERS)
            .toList();

        // 4. Redis에 캐싱
        if (!topSimilarUsers.isEmpty()) {
            cachePort.saveSimilarUsers(userId, topSimilarUsers);
        }

        log.debug("Found {} similar users for user {} (from {} candidates)",
            topSimilarUsers.size(), userId, similarities.size());

        return topSimilarUsers;
    }

    /**
     * 코사인 유사도 계산
     */
    private double calculateCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<String, Double> entry : vec1.entrySet()) {
            String key = entry.getKey();
            double val1 = entry.getValue();

            normA += val1 * val1;

            if (vec2.containsKey(key)) {
                double val2 = vec2.get(key);
                dotProduct += val1 * val2;
            }
        }

        for (double val2 : vec2.values()) {
            normB += val2 * val2;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
