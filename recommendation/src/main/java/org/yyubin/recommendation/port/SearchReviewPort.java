package org.yyubin.recommendation.port;

import java.util.List;
import org.yyubin.recommendation.search.document.ReviewDocument;

public interface SearchReviewPort {
    <S extends ReviewDocument> List<S> saveAll(Iterable<S> documents);
}
