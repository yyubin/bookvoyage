package org.yyubin.application.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.recommendation.UserAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 분석 추천 도서 검증 서비스
 * LLM이 추천한 도서가 실제로 존재하는지 외부 검색 API를 통해 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalysisValidationService {

    private final SearchBooksUseCase searchBooksUseCase;

    private static final int SEARCH_PAGE_SIZE = 5;
    private static final SearchOrder DEFAULT_SEARCH_ORDER = SearchOrder.RELEVANCE;

    /**
     * 추천 도서 목록을 검증하고 정규화
     * 존재하지 않는 도서는 필터링되고, 검색 결과로 제목과 저자가 정규화됨
     *
     * @param analysis 원본 분석 결과
     * @return 검증된 추천 도서 목록을 포함한 분석 결과
     */
    public UserAnalysis validateRecommendations(UserAnalysis analysis) {
        if (analysis.recommendations() == null || analysis.recommendations().isEmpty()) {
            return analysis;
        }

        List<UserAnalysis.BookRecommendation> validated = new ArrayList<>();
        for (UserAnalysis.BookRecommendation rec : analysis.recommendations()) {
            String query = buildRecommendationQuery(rec);
            if (query.isBlank()) {
                continue;
            }
            try {
                BookSearchPage page = searchBooksUseCase.query(
                    new SearchBooksQuery(query, 0, SEARCH_PAGE_SIZE, null, DEFAULT_SEARCH_ORDER, PrintType.ALL)
                );
                if (page.items().isEmpty()) {
                    log.debug("No search results for recommendation: {}", query);
                    continue;
                }
                BookSearchItem item = page.items().get(0);
                String author = item.getAuthors().isEmpty() ? rec.author() : String.join(", ", item.getAuthors());
                validated.add(UserAnalysis.BookRecommendation.of(item.getTitle(), author, rec.reason()));
            } catch (Exception e) {
                log.warn("Failed to validate recommendation [{}] via external search", query, e);
            }
        }

        return new UserAnalysis(
            analysis.userId(),
            analysis.personaType(),
            analysis.summary(),
            analysis.keywords(),
            validated,
            analysis.analyzedAt()
        );
    }

    /**
     * 추천 정보로부터 검색 쿼리 생성
     *
     * @param rec 추천 도서 정보
     * @return 검색 쿼리 문자열
     */
    private String buildRecommendationQuery(UserAnalysis.BookRecommendation rec) {
        String title = rec.bookTitle() == null ? "" : rec.bookTitle().trim();
        String author = rec.author() == null ? "" : rec.author().trim();
        if (title.isEmpty() && author.isEmpty()) {
            return "";
        }
        if (author.isEmpty()) {
            return title;
        }
        if (title.isEmpty()) {
            return author;
        }
        return title + " " + author;
    }
}
