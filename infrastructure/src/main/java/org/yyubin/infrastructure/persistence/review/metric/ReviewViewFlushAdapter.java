package org.yyubin.infrastructure.persistence.review.metric;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewViewFlushPort;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;

/**
 * ReviewViewFlushPort의 Infrastructure 구현체
 * - MySQL의 Review 엔티티 조회수 업데이트
 * - Elasticsearch 동기화는 배치 작업에서 별도 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewViewFlushAdapter implements ReviewViewFlushPort {

    private final ReviewJpaRepository reviewJpaRepository;
    private final JdbcTemplate jdbcTemplate;

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
    public void batchUpdateViewCount(List<CounterUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            log.debug("No updates to process");
            return;
        }

        // CASE WHEN 문을 사용한 배치 UPDATE
        // UPDATE review
        // SET view_count = view_count + CASE id
        //   WHEN 1 THEN 10
        //   WHEN 2 THEN 20
        //   ...
        // END
        // WHERE id IN (1, 2, ...)

        String caseWhen = updates.stream()
                .map(u -> String.format("WHEN %d THEN %d", u.reviewId(), u.delta()))
                .collect(Collectors.joining(" "));

        String ids = updates.stream()
                .map(u -> String.valueOf(u.reviewId()))
                .collect(Collectors.joining(","));

        String sql = String.format(
                "UPDATE review SET view_count = view_count + CASE id %s END WHERE id IN (%s)",
                caseWhen,
                ids
        );

        int rowsUpdated = jdbcTemplate.update(sql);
        log.info("Batch updated view count for {} reviews (affected rows: {})", updates.size(), rowsUpdated);
    }

    @Override
    public void updateSearchIndexViewCount(Map<Long, Long> deltas) {
        // Elasticsearch 동기화는 배치 작업(ElasticsearchSyncJob)에서 처리
        // 여기서는 no-op
    }
}
