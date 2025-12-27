package org.yyubin.infrastructure.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewSearchPort;
import org.yyubin.application.review.search.dto.ReviewSearchItemResult;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.recommendation.review.search.ReviewContentDocument;

@Component
@RequiredArgsConstructor
public class ReviewSearchAdapter implements ReviewSearchPort {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ReviewSearchPageResult search(String keyword, Long cursor, int size) {
        Query query = buildQuery(keyword, cursor, size);
        SearchHits<ReviewContentDocument> hits = elasticsearchOperations.search(query, ReviewContentDocument.class);

        List<ReviewSearchItemResult> items = new ArrayList<>();
        for (SearchHit<ReviewContentDocument> hit : hits) {
            ReviewContentDocument doc = hit.getContent();
            items.add(new ReviewSearchItemResult(
                    doc.getReviewId(),
                    doc.getBookId(),
                    doc.getUserId(),
                    doc.getSummary(),
                    doc.getHighlights(),
                    doc.getKeywords(),
                    doc.getRating(),
                    doc.getCreatedAt()
            ));
        }

        Long nextCursor = items.size() >= size
                ? items.get(items.size() - 1).reviewId()
                : null;

        return new ReviewSearchPageResult(items, nextCursor);
    }

    private Query buildQuery(String keyword, Long cursor, int size) {
        List<String> tokens = tokenize(keyword);
        StringBuilder json = new StringBuilder();
        json.append("{\"bool\":{");
        json.append("\"must\":[");
        json.append("{\"multi_match\":{");
        json.append("\"query\":\"").append(escape(keyword)).append("\",");
        json.append("\"fields\":[");
        json.append("\"bookTitle^4\",\"summary^3\",\"highlights^2\",\"content^1\",");
        json.append("\"bookTitle.ngram^0.8\",\"summary.ngram^0.5\",\"highlights.ngram^0.5\",\"content.ngram^0.3\"");
        json.append("]}");
        json.append("}");
        json.append("]");

        if (!tokens.isEmpty()) {
            json.append(",\"should\":[");
            json.append("{\"terms\":{\"keywords\":[");
            for (int i = 0; i < tokens.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append("\"").append(escape(tokens.get(i))).append("\"");
            }
            json.append("],\"boost\":2.0}}");
            json.append("]");
        }

        if (cursor != null) {
            json.append(",\"filter\":[{\"range\":{\"reviewId\":{\"lt\":").append(cursor).append("}}}]");
        }
        json.append("}}");

        Query query = new StringQuery(json.toString());
        query.setPageable(PageRequest.of(0, size));
        query.addSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("reviewId")));
        return query;
    }

    private List<String> tokenize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = keyword.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
