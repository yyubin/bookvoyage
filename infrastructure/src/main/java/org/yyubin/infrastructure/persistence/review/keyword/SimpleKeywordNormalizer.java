package org.yyubin.infrastructure.persistence.review.keyword;

import org.springframework.stereotype.Component;
import org.yyubin.domain.review.KeywordNormalizer;

@Component
public class SimpleKeywordNormalizer implements KeywordNormalizer {

    @Override
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
