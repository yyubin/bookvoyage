package org.yyubin.recommendation.adapter;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.recommendation.port.ReviewViewFlushPort;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

@Component
public class ReviewViewFlushAdapter implements ReviewViewFlushPort {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;

    public ReviewViewFlushAdapter(ReviewJpaRepository reviewJpaRepository, ReviewDocumentRepository reviewDocumentRepository) {
        this.reviewJpaRepository = reviewJpaRepository;
        this.reviewDocumentRepository = reviewDocumentRepository;
    }

    @Override
    public Optional<Long> findCurrentViewCount(Long reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .map(entity -> entity.getViewCount() != null ? entity.getViewCount() : 0L);
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
        deltas.forEach((id, delta) -> reviewDocumentRepository.findById(id.toString()).ifPresent(doc -> {
            long newCount = (doc.getViewCount() != null ? doc.getViewCount() : 0L) + delta;
            doc.setViewCount(newCount);
            reviewDocumentRepository.save(doc);
        }));
    }
}
