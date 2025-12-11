package org.yyubin.api.wishlist;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final AddWishlistUseCase addWishlistUseCase;
    private final RemoveWishlistUseCase removeWishlistUseCase;
    private final GetWishlistUseCase getWishlistUseCase;

    @PostMapping
    public ResponseEntity<Void> add(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddWishlistRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        addWishlistUseCase.add(new AddWishlistCommand(userId, toBookSearchItem(request)));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        removeWishlistUseCase.remove(new RemoveWishlistCommand(userId, bookId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> list(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        Long userId = resolveUserId(principal);
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

    private Long resolveUserId(Object principal) {
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        throw new IllegalArgumentException("Unauthorized");
    }
}
