package org.yyubin.recommendation.adapter;

import java.util.List;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.port.GraphReviewPort;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.recommendation.review.graph.ReviewNodeRepository;

/**
 * GraphReviewPort 구현체
 * Batch 모듈에서 ReviewNode를 Neo4j에 저장할 때 사용
 */
@Component
public class GraphReviewAdapter implements GraphReviewPort {

    private final ReviewNodeRepository reviewNodeRepository;

    public GraphReviewAdapter(ReviewNodeRepository reviewNodeRepository) {
        this.reviewNodeRepository = reviewNodeRepository;
    }

    @Override
    public <S extends ReviewNode> List<S> saveAll(Iterable<S> reviews) {
        List<S> result = new java.util.ArrayList<>();
        reviewNodeRepository.saveAll(reviews).forEach(result::add);
        return result;
    }
}
