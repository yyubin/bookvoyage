package org.yyubin.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.yyubin.api.review.dto.CreateReviewRequest;
import org.yyubin.api.review.dto.UpdateReviewRequest;
import org.yyubin.application.review.CreateReviewUseCase;
import org.yyubin.application.review.DeleteReviewUseCase;
import org.yyubin.application.review.GetReviewUseCase;
import org.yyubin.application.review.GetUserReviewsUseCase;
import org.yyubin.application.review.UpdateReviewUseCase;
import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.ReviewVisibility;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@DisplayName("ReviewController 테스트")
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateReviewUseCase createReviewUseCase;

    @MockitoBean
    private DeleteReviewUseCase deleteReviewUseCase;

    @MockitoBean
    private GetReviewUseCase getReviewUseCase;

    @MockitoBean
    private GetUserReviewsUseCase getUserReviewsUseCase;

    @MockitoBean
    private UpdateReviewUseCase updateReviewUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    @WithMockUser(username = "1")
    void createReview_Success() throws Exception {
        // Given
        CreateReviewRequest request = new CreateReviewRequest(
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
                "google-volume-id-123",
                5,
                "Great book about clean code practices!",
                "PUBLIC",
                "ESSAY",
                List.of("clean-code", "programming")
        );

        ReviewResult reviewResult = new ReviewResult(
                1L,                                                       // reviewId
                1L,                                                       // bookId
                "Clean Code",                                             // title
                List.of("Robert C. Martin"),                              // authors
                "0132350882",                                             // isbn10
                "9780132350884",                                          // isbn13
                "http://example.com/cover.jpg",                           // coverUrl
                "Prentice Hall",                                          // publisher
                "2008-08-01",                                             // publishedDate
                "A Handbook of Agile Software Craftsmanship",             // description
                "en",                                                     // language
                464,                                                      // pageCount
                "google-volume-id-123",                                   // googleVolumeId
                5,                                                        // rating
                "Great book about clean code practices!",                 // content
                LocalDateTime.now(),                                      // createdAt
                ReviewVisibility.PUBLIC,                                  // visibility
                false,                                                    // deleted
                0,                                                        // viewCount
                BookGenre.ESSAY,                                          // genre
                List.of("clean-code", "programming"),                     // keywords
                List.of()                                                 // mentions
        );

        when(createReviewUseCase.execute(any())).thenReturn(reviewResult);

        // When & Then
        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("Great book about clean code practices!"));

        verify(createReviewUseCase).execute(any());
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        ReviewResult reviewResult = new ReviewResult(
                reviewId,                                  // reviewId
                1L,                                        // bookId
                "Clean Code",                              // title
                List.of("Robert C. Martin"),               // authors
                "0132350882",                              // isbn10
                "9780132350884",                           // isbn13
                "http://example.com/cover.jpg",            // coverUrl
                "Prentice Hall",                           // publisher
                "2008-08-01",                              // publishedDate
                "A Handbook of Agile Software Craftsmanship", // description
                "en",                                      // language
                464,                                       // pageCount
                "google-volume-id-123",                    // googleVolumeId
                5,                                         // rating
                "Great book!",                             // content
                LocalDateTime.now(),                       // createdAt
                ReviewVisibility.PUBLIC,                   // visibility
                false,                                     // deleted
                5,                                         // viewCount
                BookGenre.ESSAY,                           // genre
                List.of("clean-code"),                     // keywords
                List.of()                                  // mentions
        );

        when(getReviewUseCase.query(any())).thenReturn(reviewResult);

        // When & Then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.likeCount").value(10))
                .andExpect(jsonPath("$.viewCount").value(5));

        verify(getReviewUseCase).query(any());
    }

    @Test
    @DisplayName("사용자별 리뷰 목록 조회 성공")
    void getUserReviews_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<ReviewResult> reviews = List.of(
                new ReviewResult(
                        1L,                                  // reviewId
                        1L,                                  // bookId
                        "Review 1",                          // title
                        List.of("Author 1"),                 // authors
                        "1234567890",                        // isbn10
                        "9781234567890",                     // isbn13
                        "http://example.com/cover1.jpg",     // coverUrl
                        "Publisher 1",                       // publisher
                        "2023-01-01",                        // publishedDate
                        "Description 1",                     // description
                        "en",                                // language
                        300,                                 // pageCount
                        "volume-1",                          // googleVolumeId
                        5,                                   // rating
                        "Content 1",                         // content
                        LocalDateTime.now(),                 // createdAt
                        ReviewVisibility.PUBLIC,             // visibility
                        false,                               // deleted
                        10,                                  // viewCount
                        BookGenre.FICTION,                   // genre
                        List.of("keyword1"),                 // keywords
                        List.of()                            // mentions
                )
        );

        PagedReviewResult pagedResult = new PagedReviewResult(reviews, 2L);
        when(getUserReviewsUseCase.query(any())).thenReturn(pagedResult);

        // When & Then
        mockMvc.perform(get("/api/reviews/users/{userId}", userId)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews[0].reviewId").value(1))
                .andExpect(jsonPath("$.nextCursor").value(2));

        verify(getUserReviewsUseCase).query(any());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    @WithMockUser(username = "1")
    void updateReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        UpdateReviewRequest request = new UpdateReviewRequest(
                "Updated Title",
                List.of("Updated Author"),
                "1111111111",
                "9781111111111",
                "http://example.com/updated-cover.jpg",
                "Updated Publisher",
                "2024-01-01",
                "Updated Description",
                "en",
                500,
                "updated-volume-id",
                4,
                "Updated content",
                "PRIVATE",
                "MYSTERY",
                List.of("updated-keyword")
        );

        ReviewResult reviewResult = new ReviewResult(
                reviewId,                                    // reviewId
                1L,                                          // bookId
                "Updated Title",                             // title
                List.of("Updated Author"),                   // authors
                "1111111111",                                // isbn10
                "9781111111111",                             // isbn13
                "http://example.com/updated-cover.jpg",      // coverUrl
                "Updated Publisher",                         // publisher
                "2024-01-01",                                // publishedDate
                "Updated Description",                       // description
                "en",                                        // language
                500,                                         // pageCount
                "updated-volume-id",                         // googleVolumeId
                4,                                           // rating
                "Updated content",                           // content
                LocalDateTime.now(),                         // createdAt
                ReviewVisibility.PRIVATE,                    // visibility
                false,                                       // deleted
                0,                                           // viewCount
                BookGenre.MYSTERY,                           // genre
                List.of("updated-keyword"),                  // keywords
                List.of()                                    // mentions
        );

        when(updateReviewUseCase.execute(any())).thenReturn(reviewResult);

        // When & Then
        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));

        verify(updateReviewUseCase).execute(any());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    @WithMockUser(username = "1")
    void deleteReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteReviewUseCase).execute(any());
    }
}
