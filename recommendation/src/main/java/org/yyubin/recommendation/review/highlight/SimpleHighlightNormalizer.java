package org.yyubin.recommendation.review.highlight;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.yyubin.domain.review.HighlightNormalizer;

@Component("recommendationHighlightNormalizer")
@Primary
public class SimpleHighlightNormalizer implements HighlightNormalizer {

    @Override
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
