package org.yyubin.application.review;

import java.util.List;
import org.yyubin.domain.review.ReviewId;

public interface RegisterKeywordsUseCase {

    void register(ReviewId reviewId, List<String> rawKeywords);
}
