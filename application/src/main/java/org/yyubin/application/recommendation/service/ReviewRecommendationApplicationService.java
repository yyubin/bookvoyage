package org.yyubin.application.recommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.GetReviewRecommendationsUseCase;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;
import org.yyubin.application.recommendation.port.ReviewRecommendationPort;
import org.yyubin.application.recommendation.query.GetReviewRecommendationsQuery;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.application.review.port.ReviewStatisticsPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.user.User;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewRecommendationApplicationService implements GetReviewRecommendationsUseCase {

    private final ReviewRecommendationPort reviewRecommendationPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadUserPort loadUserPort;
    private final LoadBookPort loadBookPort;
    private final ReviewStatisticsPort reviewStatisticsPort;
    private final ReviewReactionPort reviewReactionPort;

    @Override
    public List<ReviewRecommendationResultDto> query(GetReviewRecommendationsQuery query) {
        log.info("Getting review recommendations for user {} (cursor: {}, limit: {}, forceRefresh: {})",
                query.userId(), query.cursor(), query.limit(), query.forceRefresh());

        // 1. 추천 시스템에서 reviewId 목록 가져오기
        List<ReviewRecommendationPort.RecommendationItem> recommendations =
                reviewRecommendationPort.getRecommendations(
                        query.userId(),
                        query.cursor(),
                        query.limit(),
                        query.forceRefresh()
                );

        if (recommendations.isEmpty()) {
            log.warn("No review recommendations found for user {}", query.userId());
            return List.of();
        }

        // 2. reviewId 추출
        List<Long> reviewIds = recommendations.stream()
                .map(ReviewRecommendationPort.RecommendationItem::reviewId)
                .toList();

        // 3. Review 정보 배치 조회
        Map<Long, Review> reviewMap = loadReviewsBatch(reviewIds);

        // 4. 추가 정보 배치 조회
        List<Long> userIds = reviewMap.values().stream()
                .map(review -> review.getUserId().value())
                .distinct()
                .toList();

        List<Long> bookIds = reviewMap.values().stream()
                .map(review -> review.getBookId().getValue())
                .distinct()
                .toList();

        Map<Long, User> userMap = loadUserPort.loadByIdsBatch(userIds);
        Map<Long, Book> bookMap = loadBooksBatch(bookIds);
        Map<Long, ReviewStatisticsPort.ReviewStatistics> statisticsMap =
                reviewStatisticsPort.getBatchStatistics(reviewIds);

        // 5. 결과 조합
        List<ReviewRecommendationResultDto> results = new ArrayList<>();
        for (ReviewRecommendationPort.RecommendationItem item : recommendations) {
            Review review = reviewMap.get(item.reviewId());
            if (review == null) {
                log.warn("Review not found: {}", item.reviewId());
                continue;
            }

            User author = userMap.get(review.getUserId().value());
            Book book = bookMap.get(review.getBookId().getValue());
            ReviewStatisticsPort.ReviewStatistics stats = statisticsMap.get(item.reviewId());
            List<ReviewRecommendationResultDto.ReactionInfo> topReactions = getTopReactions(item.reviewId());

            results.add(new ReviewRecommendationResultDto(
                    review.getId() != null ? review.getId().getValue() : null,
                    review.getUserId().value(),
                    author != null ? author.nickname() : "알 수 없음",
                    review.getBookId().getValue(),
                    book != null ? book.getMetadata().getTitle() : "알 수 없음",
                    book != null ? book.getMetadata().getCoverUrl() : null,
                    review.getSummary(),
                    review.getContent(),
                    review.getRating().getValue(),
                    review.getCreatedAt(),
                    stats != null ? stats.likeCount().longValue() : 0L,
                    stats != null ? stats.commentCount().longValue() : 0L,
                    stats != null ? stats.viewCount() : 0L,
                    topReactions,
                    item.score(),
                    item.rank(),
                    item.source(),
                    item.reason()
            ));
        }

        log.info("Returning {} review recommendations for user {}", results.size(), query.userId());
        return results;
    }

    private List<ReviewRecommendationResultDto.ReactionInfo> getTopReactions(Long reviewId) {
        try {
            List<ReviewReactionPort.ReactionCount> reactions =
                    reviewReactionPort.countByReviewIdGroupByContent(reviewId);

            return reactions.stream()
                    .sorted(Comparator.comparing(ReviewReactionPort.ReactionCount::getCount).reversed())
                    .limit(3)
                    .map(r -> new ReviewRecommendationResultDto.ReactionInfo(r.getEmoji(), r.getCount()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load reactions for review {}: {}", reviewId, e.getMessage());
            return List.of();
        }
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

    private Map<Long, Review> loadReviewsBatch(List<Long> reviewIds) {
        return reviewIds.stream()
                .map(reviewId -> {
                    try {
                        return loadReviewPort.loadById(reviewId);
                    } catch (Exception e) {
                        log.warn("Failed to load review {}: {}", reviewId, e.getMessage());
                        return null;
                    }
                })
                .filter(review -> review != null)
                .collect(Collectors.toMap(
                        review -> review.getId().getValue(),
                        review -> review
                ));
    }
}
