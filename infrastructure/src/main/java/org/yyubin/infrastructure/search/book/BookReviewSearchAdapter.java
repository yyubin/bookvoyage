package org.yyubin.infrastructure.search.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.port.SearchBookReviewsPort;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

@Component
@RequiredArgsConstructor
public class BookReviewSearchAdapter implements SearchBookReviewsPort {

    private final ReviewDocumentRepository reviewDocumentRepository;

    @Override
    public SearchResult searchByBookId(Long bookId, Long cursor, int size, String sortType) {
        Sort sort = determineSortOrder(sortType);
        PageRequest pageRequest = PageRequest.of(0, size + 1, sort);

        Page<org.yyubin.recommendation.search.document.ReviewDocument> page =
                reviewDocumentRepository.findPublicReviewsByBook(bookId, pageRequest);

        List<org.yyubin.recommendation.search.document.ReviewDocument> content = page.getContent();
        Long nextCursor = content.size() > size ? (long) size : null;
        List<org.yyubin.recommendation.search.document.ReviewDocument> reviews =
                content.size() > size ? content.subList(0, size) : content;

        var mappedReviews = reviews.stream()
                .map(doc -> new SearchBookReviewsPort.ReviewDocument(
                        Long.parseLong(doc.getId()),
                        doc.getUserId(),
                        doc.getTitle(),
                        doc.getRating(),
                        doc.getContent(),
                        doc.getCreatedAt(),
                        doc.getLikeCount(),
                        doc.getCommentCount(),
                        doc.getViewCount()
                ))
                .toList();

        return new SearchResult(mappedReviews, nextCursor, page.getTotalElements());
    }

    private Sort determineSortOrder(String sortType) {
        return switch (sortType) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "likeCount", "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "dwellScore", "likeCount"); // recommended
        };
    }
}
