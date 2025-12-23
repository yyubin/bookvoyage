package org.yyubin.application.review;

import java.util.List;
import org.yyubin.domain.review.ReviewId;

public interface LoadKeywordsUseCase {

    List<String> loadKeywords(ReviewId reviewId);
}
