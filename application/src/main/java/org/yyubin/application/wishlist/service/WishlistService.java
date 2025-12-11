package org.yyubin.application.wishlist.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.wishlist.AddWishlistUseCase;
import org.yyubin.application.wishlist.GetWishlistUseCase;
import org.yyubin.application.wishlist.RemoveWishlistUseCase;
import org.yyubin.application.wishlist.command.AddWishlistCommand;
import org.yyubin.application.wishlist.command.RemoveWishlistCommand;
import org.yyubin.application.wishlist.dto.WishlistItemResult;
import org.yyubin.application.wishlist.dto.WishlistResult;
import org.yyubin.application.wishlist.port.WishlistPort;
import org.yyubin.application.wishlist.query.GetWishlistQuery;
import org.yyubin.application.wishlist.query.WishlistSort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.wishlist.Wishlist;

@Service
@RequiredArgsConstructor
public class WishlistService implements AddWishlistUseCase, RemoveWishlistUseCase, GetWishlistUseCase {

    private final WishlistPort wishlistPort;
    private final LoadBookPort loadBookPort;
    private final SaveBookPort saveBookPort;

    @Override
    @Transactional
    public void add(AddWishlistCommand command) {
        UserId userId = new UserId(command.userId());
        Book book = resolveBook(command.bookSearchItem());

        if (wishlistPort.exists(userId, book.getId())) {
            return;
        }

        wishlistPort.save(Wishlist.create(userId, book.getId()));
        // TODO: recommendation boost integration
    }

    @Override
    @Transactional
    public void remove(RemoveWishlistCommand command) {
        wishlistPort.delete(new UserId(command.userId()), BookId.of(command.bookId()));
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResult query(GetWishlistQuery query) {
        UserId userId = new UserId(query.userId());
        List<Wishlist> wishlists = wishlistPort.findByUser(userId);

        List<WishlistItemResult> items = wishlists.stream()
                .map(wishlist -> WishlistItemResult.from(
                        wishlist,
                        loadBookPort.loadById(wishlist.getBookId().getValue())
                                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + wishlist.getBookId().getValue()))
                ))
                .sorted(resolveComparator(query.sort()))
                .toList();

        return new WishlistResult(items);
    }

    private Comparator<WishlistItemResult> resolveComparator(WishlistSort sort) {
        return switch (sort) {
            case TITLE -> Comparator.comparing(WishlistItemResult::title, String.CASE_INSENSITIVE_ORDER);
            case AUTHOR -> Comparator.comparing(item -> firstAuthor(item.authors()), String.CASE_INSENSITIVE_ORDER);
            case PUBLISHED_DATE -> Comparator.comparing(WishlistItemResult::publishedDate,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case RECENT -> Comparator.comparing(WishlistItemResult::createdAt).reversed();
        };
    }

    private String firstAuthor(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return "";
        }
        return authors.get(0);
    }

    private Book resolveBook(BookSearchItem item) {
        return loadBookPort.loadByIdentifiers(item.getIsbn10(), item.getIsbn13(), item.getGoogleVolumeId())
                .orElseGet(() -> saveBookPort.save(Book.create(
                        item.getTitle(),
                        item.getAuthors(),
                        item.getIsbn10(),
                        item.getIsbn13(),
                        item.getCoverUrl(),
                        item.getPublisher(),
                        item.getPublishedDate(),
                        item.getDescription(),
                        item.getLanguage(),
                        item.getPageCount(),
                        item.getGoogleVolumeId()
                )));
    }
}
