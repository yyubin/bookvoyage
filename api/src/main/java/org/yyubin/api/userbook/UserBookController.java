package org.yyubin.api.userbook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.userbook.dto.AddUserBookRequest;
import org.yyubin.api.userbook.dto.UpdateUserBookMemoRequest;
import org.yyubin.api.userbook.dto.UpdateUserBookProgressRequest;
import org.yyubin.api.userbook.dto.UpdateUserBookRatingRequest;
import org.yyubin.api.userbook.dto.UpdateUserBookStatusRequest;
import org.yyubin.api.userbook.dto.UserBookListResponse;
import org.yyubin.api.userbook.dto.UserBookResponse;
import org.yyubin.api.userbook.dto.UserBookStatisticsResponse;
import org.yyubin.application.userbook.AddUserBookUseCase;
import org.yyubin.application.userbook.DeleteUserBookUseCase;
import org.yyubin.application.userbook.GetUserBookStatisticsUseCase;
import org.yyubin.application.userbook.GetUserBookUseCase;
import org.yyubin.application.userbook.GetUserBooksUseCase;
import org.yyubin.application.userbook.UpdateUserBookMemoUseCase;
import org.yyubin.application.userbook.UpdateUserBookProgressUseCase;
import org.yyubin.application.userbook.UpdateUserBookRatingUseCase;
import org.yyubin.application.userbook.UpdateUserBookStatusUseCase;
import org.yyubin.application.userbook.command.AddUserBookCommand;
import org.yyubin.application.userbook.command.DeleteUserBookCommand;
import org.yyubin.application.userbook.command.UpdateUserBookMemoCommand;
import org.yyubin.application.userbook.command.UpdateUserBookProgressCommand;
import org.yyubin.application.userbook.command.UpdateUserBookRatingCommand;
import org.yyubin.application.userbook.command.UpdateUserBookStatusCommand;
import org.yyubin.application.userbook.dto.UserBookResult;
import org.yyubin.application.userbook.query.GetUserBookQuery;
import org.yyubin.application.userbook.query.GetUserBookStatisticsQuery;
import org.yyubin.application.userbook.query.GetUserBooksQuery;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.user.UserId;

@RestController
@RequestMapping("/api/user-books")
@RequiredArgsConstructor
public class UserBookController {

    private final AddUserBookUseCase addUserBookUseCase;
    private final GetUserBooksUseCase getUserBooksUseCase;
    private final GetUserBookUseCase getUserBookUseCase;
    private final UpdateUserBookStatusUseCase updateUserBookStatusUseCase;
    private final UpdateUserBookProgressUseCase updateUserBookProgressUseCase;
    private final UpdateUserBookRatingUseCase updateUserBookRatingUseCase;
    private final UpdateUserBookMemoUseCase updateUserBookMemoUseCase;
    private final DeleteUserBookUseCase deleteUserBookUseCase;
    private final GetUserBookStatisticsUseCase getUserBookStatisticsUseCase;

    @PostMapping
    public ResponseEntity<UserBookResponse> add(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody AddUserBookRequest request
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        AddUserBookCommand command = new AddUserBookCommand(
                userId.value(),
                toBookSearchItem(request),
                request.status()
        );
        UserBookResult result = addUserBookUseCase.execute(command);
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<UserBookListResponse> list(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "status", required = false) String status
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        return ResponseEntity.ok(
                UserBookListResponse.from(
                        getUserBooksUseCase.query(new GetUserBooksQuery(userId.value(), status))
                )
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<UserBookResponse> detail(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        UserBookResult result = getUserBookUseCase.query(new GetUserBookQuery(userId.value(), bookId));
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @PutMapping("/{bookId}/status")
    public ResponseEntity<UserBookResponse> updateStatus(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateUserBookStatusRequest request
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        UserBookResult result = updateUserBookStatusUseCase.execute(
                new UpdateUserBookStatusCommand(userId.value(), bookId, request.status())
        );
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @PutMapping("/{bookId}/progress")
    public ResponseEntity<UserBookResponse> updateProgress(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateUserBookProgressRequest request
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        UserBookResult result = updateUserBookProgressUseCase.execute(
                new UpdateUserBookProgressCommand(userId.value(), bookId, request.progress())
        );
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @PutMapping("/{bookId}/rating")
    public ResponseEntity<UserBookResponse> updateRating(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateUserBookRatingRequest request
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        UserBookResult result = updateUserBookRatingUseCase.execute(
                new UpdateUserBookRatingCommand(userId.value(), bookId, request.rating())
        );
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @PutMapping("/{bookId}/memo")
    public ResponseEntity<UserBookResponse> updateMemo(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateUserBookMemoRequest request
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        UserBookResult result = updateUserBookMemoUseCase.execute(
                new UpdateUserBookMemoCommand(userId.value(), bookId, request.memo())
        );
        return ResponseEntity.ok(UserBookResponse.from(result));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long bookId
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        deleteUserBookUseCase.execute(new DeleteUserBookCommand(userId.value(), bookId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<UserBookStatisticsResponse> statistics(
            @AuthenticationPrincipal Object principal
    ) {
        UserId userId = new UserId(PrincipalUtils.requireUserId(principal));
        return ResponseEntity.ok(
                UserBookStatisticsResponse.from(
                        getUserBookStatisticsUseCase.query(new GetUserBookStatisticsQuery(userId.value()))
                )
        );
    }

    private BookSearchItem toBookSearchItem(AddUserBookRequest request) {
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
import org.yyubin.api.common.PrincipalUtils;
