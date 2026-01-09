package org.yyubin.application.book;

import org.yyubin.application.book.dto.ShelfAdditionTrendResult;
import org.yyubin.application.book.query.GetShelfAdditionTrendQuery;

public interface GetShelfAdditionTrendUseCase {
    ShelfAdditionTrendResult query(GetShelfAdditionTrendQuery query);
}
