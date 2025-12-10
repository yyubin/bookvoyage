package org.yyubin.application.review.port;

import java.util.List;
import org.yyubin.domain.review.KeywordId;
import org.yyubin.domain.review.ReviewKeyword;

public interface ReviewKeywordRepository {

    void saveAll(List<ReviewKeyword> mappings);

    void deleteAllByReviewId(Long reviewId);

    List<ReviewKeyword> findByReviewId(Long reviewId);
}
