package org.yyubin.application.review.port;

import java.util.List;
import java.util.Optional;
import org.yyubin.domain.review.Keyword;
import org.yyubin.domain.review.KeywordId;

public interface KeywordRepository {

    Optional<Keyword> findByNormalizedValue(String normalizedValue);

    Keyword save(Keyword keyword);

    List<Keyword> findAllByIds(List<KeywordId> ids);
}
