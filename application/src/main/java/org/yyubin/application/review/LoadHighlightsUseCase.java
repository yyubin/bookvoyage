package org.yyubin.application.review;

import java.util.List;
import org.yyubin.domain.review.ReviewId;

public interface LoadHighlightsUseCase {

    List<String> loadHighlights(ReviewId reviewId);
}
