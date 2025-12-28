package org.yyubin.infrastructure.search.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.port.SearchBookReviewsPort;
import org.yyubin.recommendation.review.search.ReviewContentDocument;
import org.yyubin.recommendation.review.search.ReviewContentRepository;

@Component
@RequiredArgsConstructor
public class BookReviewSearchAdapter implements SearchBookReviewsPort {

    private final ReviewContentRepository reviewContentRepository;

    @Override
    public SearchResult searchByBookId(Long bookId, Long cursor, int size, String sortType) {
        Sort sort = determineSortOrder(sortType);
        PageRequest pageRequest = PageRequest.of(0, size + 1, sort);

        Page<ReviewContentDocument> page = reviewContentRepository.findByBookId(bookId, pageRequest);

        List<ReviewContentDocument> content = page.getContent();
        Long nextCursor = content.size() > size ? (long) size : null;
        List<ReviewContentDocument> reviews = content.size() > size ? content.subList(0, size) : content;

        var mappedReviews = reviews.stream()
                .map(doc -> new SearchBookReviewsPort.ReviewDocument(
                        doc.getReviewId(),
                        doc.getUserId(),
                        doc.getSummary(),  // title 대신 summary 사용
                        doc.getRating() != null ? doc.getRating().floatValue() : null,
                        doc.getContent(),
                        doc.getCreatedAt(),
                        0,  // likeCount는 ReviewContentDocument에 없음
                        0,  // commentCount도 없음
                        0L  // viewCount도 없음
                ))
                .toList();

        return new SearchResult(mappedReviews, nextCursor, page.getTotalElements());
    }

    private Sort determineSortOrder(String sortType) {
        return switch (sortType) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "rating"); // ReviewContentDocument에는 likeCount 없음
            default -> Sort.by(Sort.Direction.DESC, "rating", "createdAt"); // recommended
        };
    }
}
