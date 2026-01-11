package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 사용자 취향 벡터
 * 사용자의 독서 취향을 키워드/장르 가중치로 표현
 * Value Object - 불변 객체
 */
public record UserTasteVector(
    Long userId,
    Map<String, Double> vector,  // keyword/genre -> weight
    LocalDateTime calculatedAt
) {

    public UserTasteVector {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(vector, "vector cannot be null");
        Objects.requireNonNull(calculatedAt, "calculatedAt cannot be null");
    }

    public static UserTasteVector of(Long userId, Map<String, Double> vector) {
        return new UserTasteVector(userId, vector, LocalDateTime.now());
    }

    /**
     * 다른 사용자와의 코사인 유사도 계산
     */
    public double cosineSimilarity(UserTasteVector other) {
        if (!this.userId.equals(other.userId())) {
            return calculateCosineSimilarity(this.vector, other.vector());
        }
        return 1.0;
    }

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

    /**
     * 상위 N개의 주요 취향 키워드 추출
     */
    public java.util.List<String> getTopKeywords(int limit) {
        return vector.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
    }
}
