package org.yyubin.recommendation.adapter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.port.SearchReviewPort;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

@Component
public class SearchReviewAdapter implements SearchReviewPort {

    private final ReviewDocumentRepository reviewDocumentRepository;

    public SearchReviewAdapter(ReviewDocumentRepository reviewDocumentRepository) {
        this.reviewDocumentRepository = reviewDocumentRepository;
    }

    @Override
    public <S extends ReviewDocument> List<S> saveAll(Iterable<S> documents) {
        List<S> result = new ArrayList<>();
        reviewDocumentRepository.saveAll(documents).forEach(result::add);
        return result;
    }
}
