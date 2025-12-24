package org.yyubin.application.review.port;

import java.util.List;
import java.util.Optional;
import org.yyubin.domain.review.Highlight;
import org.yyubin.domain.review.HighlightId;

public interface HighlightRepository {

    Optional<Highlight> findByNormalizedValue(String normalizedValue);

    Highlight save(Highlight highlight);

    List<Highlight> findAllByIds(List<HighlightId> ids);
}
