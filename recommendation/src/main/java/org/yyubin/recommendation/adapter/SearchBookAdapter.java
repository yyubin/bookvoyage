package org.yyubin.recommendation.adapter;

import java.util.List;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.port.SearchBookPort;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

/**
 * SearchBookPort 구현체
 * Batch 모듈에서 BookDocument를 Elasticsearch에 저장할 때 사용
 */
@Component
public class SearchBookAdapter implements SearchBookPort {

    private final BookDocumentRepository bookDocumentRepository;

    public SearchBookAdapter(BookDocumentRepository bookDocumentRepository) {
        this.bookDocumentRepository = bookDocumentRepository;
    }

    @Override
    public <S extends BookDocument> List<S> saveAll(Iterable<S> documents) {
        List<S> result = new java.util.ArrayList<>();
        bookDocumentRepository.saveAll(documents).forEach(result::add);
        return result;
    }
}
