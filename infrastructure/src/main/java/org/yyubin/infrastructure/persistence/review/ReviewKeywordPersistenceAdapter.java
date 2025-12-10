package org.yyubin.infrastructure.persistence.review;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewKeywordRepository;
import org.yyubin.domain.review.ReviewKeyword;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewKeywordPersistenceAdapter implements ReviewKeywordRepository {

    private final ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    @Override
    @Transactional
    public void saveAll(List<ReviewKeyword> mappings) {
        List<ReviewKeywordEntity> entities = mappings.stream()
                .map(ReviewKeywordEntity::fromDomain)
                .toList();
        reviewKeywordJpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteAllByReviewId(Long reviewId) {
        reviewKeywordJpaRepository.deleteByIdReviewId(reviewId);
    }

    @Override
    public List<ReviewKeyword> findByReviewId(Long reviewId) {
        return reviewKeywordJpaRepository.findByIdReviewId(reviewId).stream()
                .map(ReviewKeywordEntity::toDomain)
                .toList();
    }
}
