package org.yyubin.application.userbook.dto;

import java.util.List;

public record UserBookListResult(
        List<UserBookResult> items
) {
}
