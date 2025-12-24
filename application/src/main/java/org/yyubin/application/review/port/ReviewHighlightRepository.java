package org.yyubin.application.review.port;

import java.util.List;
import org.yyubin.domain.review.ReviewHighlight;

public interface ReviewHighlightRepository {

    void saveAll(List<ReviewHighlight> mappings);

    void deleteAllByReviewId(Long reviewId);

    List<ReviewHighlight> findByReviewId(Long reviewId);
}
