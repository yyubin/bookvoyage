package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 신선도 기반 스코어러
 * - 최근 출간 도서에 가산점
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FreshnessScorer implements Scorer {

    private final BookDocumentRepository bookDocumentRepository;

    @Override
    public double score(Long userId, RecommendationCandidate candidate) {
        try {
            Optional<BookDocument> bookOpt = bookDocumentRepository
                    .findById(String.valueOf(candidate.getBookId()));

            if (bookOpt.isPresent() && bookOpt.get().getPublishedDate() != null) {
                LocalDate publishedDate = bookOpt.get().getPublishedDate();
                LocalDate now = LocalDate.now();

                // 출간일로부터 경과 일수
                long daysSincePublished = ChronoUnit.DAYS.between(publishedDate, now);

                // 최근 1년 이내는 높은 점수
                if (daysSincePublished < 365) {
                    return 1.0 - (daysSincePublished / 365.0);
                } else if (daysSincePublished < 365 * 3) {
                    // 3년 이내는 중간 점수
                    return 0.5 - ((daysSincePublished - 365) / (365.0 * 2)) * 0.5;
                } else {
                    // 3년 이상은 낮은 점수
                    return 0.1;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate freshness for book {}", candidate.getBookId(), e);
        }

        return 0.5; // 기본값
    }

    @Override
    public String getName() {
        return "FreshnessScorer";
    }

    @Override
    public double getDefaultWeight() {
        return 0.05; // 신선도 점수 가중치 5%
    }
}
