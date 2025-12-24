package org.yyubin.application.review.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.RegisterHighlightsUseCase;
import org.yyubin.application.review.port.HighlightRepository;
import org.yyubin.application.review.port.ReviewHighlightRepository;
import org.yyubin.domain.review.Highlight;
import org.yyubin.domain.review.HighlightId;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.domain.review.ReviewHighlight;
import org.yyubin.domain.review.ReviewId;

@Service
@RequiredArgsConstructor
public class RegisterHighlightsService implements RegisterHighlightsUseCase, LoadHighlightsUseCase {

    private final HighlightRepository highlightRepository;
    private final ReviewHighlightRepository reviewHighlightRepository;
    private final HighlightNormalizer highlightNormalizer;

    @Override
    @Transactional
    public void register(ReviewId reviewId, List<String> rawHighlights) {
        if (rawHighlights == null || rawHighlights.isEmpty()) {
            reviewHighlightRepository.deleteAllByReviewId(reviewId.getValue());
            return;
        }

        List<ReviewHighlight> mappings = new ArrayList<>();

        for (String raw : rawHighlights) {
            Highlight highlight = highlightRepository.findByNormalizedValue(highlightNormalizer.normalize(raw))
                    .orElseGet(() -> highlightRepository.save(Highlight.create(raw, highlightNormalizer)));
            mappings.add(new ReviewHighlight(reviewId, highlight.getId()));
        }

        reviewHighlightRepository.deleteAllByReviewId(reviewId.getValue());
        reviewHighlightRepository.saveAll(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> loadHighlights(ReviewId reviewId) {
        List<ReviewHighlight> mappings = reviewHighlightRepository.findByReviewId(reviewId.getValue());
        List<HighlightId> highlightIds = mappings.stream().map(ReviewHighlight::highlightId).toList();
        return highlightRepository.findAllByIds(highlightIds).stream()
                .map(Highlight::getRawValue)
                .toList();
    }
}
