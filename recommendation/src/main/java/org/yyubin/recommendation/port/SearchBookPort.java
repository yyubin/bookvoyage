package org.yyubin.recommendation.port;

import java.util.List;
import org.yyubin.recommendation.search.document.BookDocument;

public interface SearchBookPort {
    <S extends BookDocument> List<S> saveAll(Iterable<S> documents);
}
