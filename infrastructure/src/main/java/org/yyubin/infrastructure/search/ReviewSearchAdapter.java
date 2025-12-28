package org.yyubin.infrastructure.search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewSearchPort;
import org.yyubin.application.review.search.dto.ReviewSearchFilter;
import org.yyubin.application.review.search.dto.ReviewSearchItemResult;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.ReviewSortOption;
import org.yyubin.recommendation.review.search.ReviewContentDocument;

@Component
@RequiredArgsConstructor
public class ReviewSearchAdapter implements ReviewSearchPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ReviewSearchPageResult search(String keyword, Long cursor, int size) {
        Query query = buildQuery(keyword, cursor, size);
        return executeSearch(query, size);
    }

    @Override
    public ReviewSearchPageResult searchWithFilters(ReviewSearchFilter filter) {
        Query query = buildQueryWithFilters(filter);
        return executeSearch(query, filter.size());
    }

    private ReviewSearchPageResult executeSearch(Query query, int size) {
        SearchHits<ReviewContentDocument> hits = elasticsearchOperations.search(query, ReviewContentDocument.class);

        List<ReviewSearchItemResult> items = new ArrayList<>();
        for (SearchHit<ReviewContentDocument> hit : hits) {
            ReviewContentDocument doc = hit.getContent();
            items.add(new ReviewSearchItemResult(
                    doc.getReviewId(),
                    doc.getBookId(),
                    doc.getBookTitle(),
                    doc.getUserId(),
                    doc.getAuthorNickname(),
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

    private Query buildQueryWithFilters(ReviewSearchFilter filter) {
        List<String> tokens = tokenize(filter.keyword());
        StringBuilder json = new StringBuilder();
        json.append("{\"bool\":{");

        // 1. must: 키워드 검색
        json.append("\"must\":[");
        json.append("{\"multi_match\":{");
        json.append("\"query\":\"").append(escape(filter.keyword())).append("\",");
        json.append("\"fields\":[");
        json.append("\"bookTitle^4\",\"summary^3\",\"highlights^2\",\"content^1\",");
        json.append("\"bookTitle.ngram^0.8\",\"summary.ngram^0.5\",\"highlights.ngram^0.5\",\"content.ngram^0.3\"");
        json.append("]}");
        json.append("}");
        json.append("]");

        // 2. should: 키워드 매칭 부스트
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

        // 3. filter: 필터 조건들
        List<String> filters = new ArrayList<>();

        if (filter.genre() != null) {
            filters.add("{\"term\":{\"genre\":\"" + escape(filter.genre()) + "\"}}");
        }

        if (filter.minRating() != null || filter.maxRating() != null) {
            StringBuilder ratingRange = new StringBuilder("{\"range\":{\"rating\":{");
            boolean hasMin = filter.minRating() != null;
            boolean hasMax = filter.maxRating() != null;

            if (hasMin) {
                ratingRange.append("\"gte\":").append(filter.minRating());
            }
            if (hasMax) {
                if (hasMin) ratingRange.append(",");
                ratingRange.append("\"lte\":").append(filter.maxRating());
            }
            ratingRange.append("}}}");
            filters.add(ratingRange.toString());
        }

        if (filter.startDate() != null || filter.endDate() != null) {
            StringBuilder dateRange = new StringBuilder("{\"range\":{\"createdAt\":{");
            boolean hasStart = filter.startDate() != null;
            boolean hasEnd = filter.endDate() != null;

            if (hasStart) {
                dateRange.append("\"gte\":\"").append(filter.startDate().format(ISO_FORMATTER)).append("\"");
            }
            if (hasEnd) {
                if (hasStart) dateRange.append(",");
                dateRange.append("\"lte\":\"").append(filter.endDate().format(ISO_FORMATTER)).append("\"");
            }
            dateRange.append("}}}");
            filters.add(dateRange.toString());
        }

        if (filter.highlightNorm() != null) {
            filters.add("{\"term\":{\"highlightsNorm\":\"" + escape(filter.highlightNorm()) + "\"}}");
        }

        if (filter.bookId() != null) {
            filters.add("{\"term\":{\"bookId\":" + filter.bookId() + "}}");
        }

        if (filter.userId() != null) {
            filters.add("{\"term\":{\"userId\":" + filter.userId() + "}}");
        }

        if (filter.cursor() != null) {
            filters.add("{\"range\":{\"reviewId\":{\"lt\":" + filter.cursor() + "}}}");
        }

        if (!filters.isEmpty()) {
            json.append(",\"filter\":[");
            json.append(String.join(",", filters));
            json.append("]");
        }

        json.append("}}");

        Query query = new StringQuery(json.toString());
        query.setPageable(PageRequest.of(0, filter.size()));

        // 정렬 적용
        applySorting(query, filter.sortBy());

        return query;
    }

    private void applySorting(Query query, ReviewSortOption sortBy) {
        if (sortBy == null || sortBy == ReviewSortOption.RELEVANCE) {
            // _score desc (기본)
            query.addSort(Sort.by(Sort.Order.desc("_score")));
            query.addSort(Sort.by(Sort.Order.desc("reviewId"))); // 동점 처리
        } else if (sortBy == ReviewSortOption.LATEST) {
            query.addSort(Sort.by(Sort.Order.desc("createdAt")));
            query.addSort(Sort.by(Sort.Order.desc("reviewId")));
        } else if (sortBy == ReviewSortOption.RATING_DESC) {
            query.addSort(Sort.by(Sort.Order.desc("rating")));
            query.addSort(Sort.by(Sort.Order.desc("reviewId")));
        } else if (sortBy == ReviewSortOption.RATING_ASC) {
            query.addSort(Sort.by(Sort.Order.asc("rating")));
            query.addSort(Sort.by(Sort.Order.desc("reviewId")));
        }
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
