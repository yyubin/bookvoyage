package org.yyubin.application.review;

import java.util.List;
import org.yyubin.domain.review.ReviewId;

public interface RegisterHighlightsUseCase {

    void register(ReviewId reviewId, List<String> rawHighlights);
}
