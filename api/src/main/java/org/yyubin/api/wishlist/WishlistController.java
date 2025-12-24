package org.yyubin.api.wishlist;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.wishlist.dto.AddWishlistRequest;
import org.yyubin.api.wishlist.dto.WishlistResponse;
import org.yyubin.application.wishlist.AddWishlistUseCase;
import org.yyubin.application.wishlist.GetWishlistUseCase;
import org.yyubin.application.wishlist.RemoveWishlistUseCase;
import org.yyubin.application.wishlist.command.AddWishlistCommand;
import org.yyubin.application.wishlist.command.RemoveWishlistCommand;
import org.yyubin.application.wishlist.dto.WishlistResult;
import org.yyubin.application.wishlist.query.GetWishlistQuery;
import org.yyubin.application.wishlist.query.WishlistSort;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.domain.user.UserId;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final AddWishlistUseCase addWishlistUseCase;
    private final RemoveWishlistUseCase removeWishlistUseCase;
    private final GetWishlistUseCase getWishlistUseCase;

    @PostMapping
    public ResponseEntity<Void> add(
            @AuthenticationPrincipal Object userDetails,
            @Valid @RequestBody AddWishlistRequest request
    ) {
        addWishlistUseCase.add(new AddWishlistCommand(PrincipalUtils.requireUserId(userDetails), toBookSearchItem(request)));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal Object userDetails,
            @PathVariable Long bookId
    ) {
        removeWishlistUseCase.remove(new RemoveWishlistCommand(PrincipalUtils.requireUserId(userDetails), bookId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> list(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        Long userId = PrincipalUtils.requireUserId(principal);
        WishlistResult result = getWishlistUseCase.query(new GetWishlistQuery(userId, WishlistSort.from(sort)));
        return ResponseEntity.ok(WishlistResponse.from(result));
    }

    private BookSearchItem toBookSearchItem(AddWishlistRequest request) {
        return BookSearchItem.of(
                request.title(),
                request.authors(),
                request.isbn10(),
                request.isbn13(),
                request.coverUrl(),
                request.publisher(),
                request.publishedDate(),
                request.description(),
                request.language(),
                request.pageCount(),
                request.googleVolumeId()
        );
    }

}
