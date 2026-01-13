package org.yyubin.recommendation.port;

import java.util.List;
import org.yyubin.recommendation.review.graph.ReviewNode;

public interface GraphReviewPort {
    <S extends ReviewNode> List<S> saveAll(Iterable<S> reviews);
}
