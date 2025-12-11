package org.yyubin.infrastructure.external.book.dto;

public record GoogleBookItemResponse(
        String id,
        GoogleBookVolumeInfoResponse volumeInfo
) {
}
