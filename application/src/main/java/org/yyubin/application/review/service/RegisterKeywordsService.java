package org.yyubin.application.review.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.RegisterKeywordsUseCase;
import org.yyubin.application.review.port.KeywordRepository;
import org.yyubin.application.review.port.ReviewKeywordRepository;
import org.yyubin.domain.review.Keyword;
import org.yyubin.domain.review.KeywordNormalizer;
import org.yyubin.domain.review.KeywordId;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewKeyword;

@Service
@RequiredArgsConstructor
public class RegisterKeywordsService implements RegisterKeywordsUseCase, LoadKeywordsUseCase {

    private final KeywordRepository keywordRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final KeywordNormalizer keywordNormalizer;

    @Override
    @Transactional
    public void register(ReviewId reviewId, List<String> rawKeywords) {
        if (rawKeywords == null || rawKeywords.isEmpty()) {
            reviewKeywordRepository.deleteAllByReviewId(reviewId.getValue());
            return;
        }

        List<ReviewKeyword> mappings = new ArrayList<>();

        for (String raw : rawKeywords) {
            Keyword keyword = keywordRepository.findByNormalizedValue(keywordNormalizer.normalize(raw))
                    .orElseGet(() -> keywordRepository.save(Keyword.create(raw, keywordNormalizer)));

            mappings.add(new ReviewKeyword(reviewId, keyword.getId()));
        }

        reviewKeywordRepository.deleteAllByReviewId(reviewId.getValue());
        reviewKeywordRepository.saveAll(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> loadKeywords(ReviewId reviewId) {
        List<ReviewKeyword> mappings = reviewKeywordRepository.findByReviewId(reviewId.getValue());
        List<KeywordId> keywordIds = mappings.stream().map(ReviewKeyword::keywordId).toList();
        return keywordRepository.findAllByIds(keywordIds).stream()
                .map(Keyword::getRawValue)
                .toList();
    }
}
