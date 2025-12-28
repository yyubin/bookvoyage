package org.yyubin.application.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.GetBookRecommendationsUseCase;
import org.yyubin.application.recommendation.dto.BookRecommendationResult;
import org.yyubin.application.recommendation.port.BookRecommendationPort;
import org.yyubin.application.recommendation.query.GetBookRecommendationsQuery;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.domain.book.Book;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRecommendationService implements GetBookRecommendationsUseCase {

    private final BookRecommendationPort bookRecommendationPort;
    private final LoadBookPort loadBookPort;

    @Override
    public List<BookRecommendationResult> query(GetBookRecommendationsQuery query) {
        log.info("Getting book recommendations for user {} (limit: {}, forceRefresh: {})",
                query.userId(), query.limit(), query.forceRefresh());

        // 1. 추천 시스템에서 bookId 목록 가져오기
        List<BookRecommendationPort.RecommendationItem> recommendations =
                bookRecommendationPort.getRecommendations(
                        query.userId(),
                        query.limit(),
                        query.forceRefresh()
                );

        if (recommendations.isEmpty()) {
            log.warn("No recommendations found for user {}", query.userId());
            return List.of();
        }

        // 2. bookId 추출
        List<Long> bookIds = recommendations.stream()
                .map(BookRecommendationPort.RecommendationItem::bookId)
                .toList();

        // 3. Book 정보 배치 조회
        Map<Long, Book> bookMap = loadBooksBatch(bookIds);

        // 4. 결과 조합
        List<BookRecommendationResult> results = new ArrayList<>();
        for (BookRecommendationPort.RecommendationItem item : recommendations) {
            Book book = bookMap.get(item.bookId());
            if (book == null) {
                log.warn("Book not found: {}", item.bookId());
                continue;
            }

            results.add(BookRecommendationResult.from(
                    book,
                    item.score(),
                    item.rank(),
                    item.source(),
                    item.reason()
            ));
        }

        log.info("Returning {} book recommendations for user {}", results.size(), query.userId());
        return results;
    }

    private Map<Long, Book> loadBooksBatch(List<Long> bookIds) {
        return bookIds.stream()
                .map(bookId -> {
                    try {
                        return loadBookPort.loadById(bookId).orElse(null);
                    } catch (Exception e) {
                        log.warn("Failed to load book {}: {}", bookId, e.getMessage());
                        return null;
                    }
                })
                .filter(book -> book != null)
                .collect(Collectors.toMap(
                        book -> book.getId().getValue(),
                        book -> book
                ));
    }
}
