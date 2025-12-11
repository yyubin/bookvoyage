package org.yyubin.api.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddWishlistRequest(
        @NotBlank(message = "title is required")
        String title,

        @NotEmpty(message = "authors are required")
        List<String> authors,

        String isbn10,
        String isbn13,
        String coverUrl,
        String publisher,
        String publishedDate,
        String description,
        String language,
        Integer pageCount,
        String googleVolumeId
) {
}
