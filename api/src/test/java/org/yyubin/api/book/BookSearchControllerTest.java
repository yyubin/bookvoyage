package org.yyubin.api.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.domain.book.BookSearchItem;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@DisplayName("BookSearchController 테스트")
class BookSearchControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SearchBooksUseCase searchBooksUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("도서 검색 성공")
    void searchBooks_Success() throws Exception {
        // Given
        List<BookSearchItem> books = List.of(
                BookSearchItem.of(
                        "Clean Code",
                        List.of("Robert C. Martin"),
                        "0132350882",
                        "9780132350884",
                        "http://example.com/cover.jpg",
                        "Prentice Hall",
                        "2008-08-01",
                        "A Handbook of Agile Software Craftsmanship",
                        "en",
                        464,
                        "google-volume-id-123"
                )
        );

        BookSearchPage page = new BookSearchPage(books, 40, 1);
        when(searchBooksUseCase.query(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "clean code")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.books[0].authors[0]").value("Robert C. Martin"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.startIndex").value(40));

        verify(searchBooksUseCase).query(any());
    }

    @Test
    @DisplayName("도서 검색 결과 없음")
    void searchBooks_NoResults() throws Exception {
        // Given
        BookSearchPage emptyPage = new BookSearchPage(List.of(), null, 0);
        when(searchBooksUseCase.query(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "nonexistent book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(searchBooksUseCase).query(any());
    }

    @Test
    @DisplayName("도서 검색 - 다양한 파라미터")
    void searchBooks_WithVariousParameters() throws Exception {
        // Given
        List<BookSearchItem> books = List.of(
                BookSearchItem.of(
                        "Test Book",
                        List.of("Test Author"),
                        "1234567890",
                        "9781234567890",
                        "http://example.com/test.jpg",
                        "Test Publisher",
                        "2024-01-01",
                        "Test Description",
                        "ko",
                        200,
                        "test-volume-id"
                )
        );

        BookSearchPage page = new BookSearchPage(books, 20, 1);
        when(searchBooksUseCase.query(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "test")
                        .param("startIndex", "10")
                        .param("size", "10")
                        .param("language", "ko")
                        .param("orderBy", "newest")
                        .param("printType", "books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.books[0].language").value("ko"))
                .andExpect(jsonPath("$.nextStartIndex").value(20));

        verify(searchBooksUseCase).query(any());
    }
}
