package org.yyubin.application.recommendation.port.out;

import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.domain.recommendation.SimilarUser;
import org.yyubin.domain.recommendation.UserTasteVector;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 서클 캐시 포트
 */
public interface ReviewCircleCachePort {

    /**
     * 사용자 취향 벡터 저장
     */
    void saveTasteVector(UserTasteVector tasteVector);

    /**
     * 사용자 취향 벡터 조회
     */
    Optional<UserTasteVector> getTasteVector(Long userId);

    /**
     * 유사 사용자 목록 저장
     */
    void saveSimilarUsers(Long userId, List<SimilarUser> similarUsers);

    /**
     * 유사 사용자 목록 조회
     */
    List<SimilarUser> getSimilarUsers(Long userId);

    /**
     * 리뷰 서클 저장
     */
    void saveReviewCircle(ReviewCircle reviewCircle);

    /**
     * 리뷰 서클 조회
     */
    Optional<ReviewCircle> getReviewCircle(Long userId, String window);

    /**
     * 사용자 취향 벡터 삭제
     */
    void deleteTasteVector(Long userId);

    /**
     * 유사 사용자 목록 삭제
     */
    void deleteSimilarUsers(Long userId);

    /**
     * 리뷰 서클 삭제
     */
    void deleteReviewCircle(Long userId, String window);
}
