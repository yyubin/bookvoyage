package org.yyubin.infrastructure.persistence.review.highlight;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewHighlightRepository;
import org.yyubin.domain.review.ReviewHighlight;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewHighlightPersistenceAdapter implements ReviewHighlightRepository {

    private final ReviewHighlightJpaRepository reviewHighlightJpaRepository;

    @Override
    @Transactional
    public void saveAll(List<ReviewHighlight> mappings) {
        List<ReviewHighlightEntity> entities = mappings.stream()
                .map(ReviewHighlightEntity::fromDomain)
                .toList();
        reviewHighlightJpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteAllByReviewId(Long reviewId) {
        reviewHighlightJpaRepository.deleteByIdReviewId(reviewId);
    }

    @Override
    public List<ReviewHighlight> findByReviewId(Long reviewId) {
        return reviewHighlightJpaRepository.findByIdReviewId(reviewId).stream()
                .map(ReviewHighlightEntity::toDomain)
                .toList();
    }
}
