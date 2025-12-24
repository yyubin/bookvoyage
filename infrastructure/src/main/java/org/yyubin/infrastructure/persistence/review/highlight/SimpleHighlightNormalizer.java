package org.yyubin.infrastructure.persistence.review.highlight;

import org.springframework.stereotype.Component;
import org.yyubin.domain.review.HighlightNormalizer;

@Component
public class SimpleHighlightNormalizer implements HighlightNormalizer {

    @Override
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
