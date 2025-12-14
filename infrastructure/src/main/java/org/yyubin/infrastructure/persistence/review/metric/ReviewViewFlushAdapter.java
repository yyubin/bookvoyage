package org.yyubin.infrastructure.persistence.review.metric;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewViewFlushPort;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;

/**
 * ReviewViewFlushPort의 Infrastructure 구현체
 * - MySQL의 Review 엔티티 조회수 업데이트
 * - Elasticsearch 동기화는 배치 작업에서 별도 처리
 */
@Component
@RequiredArgsConstructor
public class ReviewViewFlushAdapter implements ReviewViewFlushPort {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Optional<Long> findCurrentViewCount(Long reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .map(ReviewEntity::getViewCount)
                .map(count -> count != null ? count : 0L);
    }

    @Override
    public void updateViewCount(Long reviewId, long newCount) {
        reviewJpaRepository.findById(reviewId).ifPresent(entity -> {
            entity.setViewCount(newCount);
            reviewJpaRepository.save(entity);
        });
    }

    @Override
    public void updateSearchIndexViewCount(Map<Long, Long> deltas) {
        // Elasticsearch 동기화는 배치 작업(ElasticsearchSyncJob)에서 처리
        // 여기서는 no-op
    }
}
