package org.yyubin.recommendation.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FreshnessScorer 테스트")
class FreshnessScorerTest {

    @Mock
    private BookDocumentRepository bookDocumentRepository;

    @InjectMocks
    private FreshnessScorer freshnessScorer;

    private RecommendationCandidate candidate;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.8)
                .build();
    }

    @Test
    @DisplayName("최근 출간 도서 (1개월 전) - 높은 점수")
    void score_RecentBook_HighScore() {
        // Given
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        BookDocument bookDocument = createBookDocument(oneMonthAgo);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isGreaterThan(0.9);
        assertThat(score).isLessThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("6개월 전 출간 도서 - 중간 점수")
    void score_SixMonthsOldBook_MediumScore() {
        // Given
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        BookDocument bookDocument = createBookDocument(sixMonthsAgo);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isGreaterThan(0.4);
        assertThat(score).isLessThan(0.6);
    }

    @Test
    @DisplayName("1년 전 출간 도서 - 중간 점수")
    void score_OneYearOldBook_LowerMediumScore() {
        // Given
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        BookDocument bookDocument = createBookDocument(oneYearAgo);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        // 정확히 1년 전 (365일)이므로 0.5 - ((365-365)/(365*2)) * 0.5 = 0.5
        assertThat(score).isCloseTo(0.5, within(0.05));
    }

    @Test
    @DisplayName("2년 전 출간 도서 - 낮은 점수")
    void score_TwoYearsOldBook_LowScore() {
        // Given
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        BookDocument bookDocument = createBookDocument(twoYearsAgo);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isGreaterThan(0.2);
        assertThat(score).isLessThan(0.5);
    }

    @Test
    @DisplayName("3년 이상 전 출간 도서 - 매우 낮은 점수")
    void score_VeryOldBook_VeryLowScore() {
        // Given
        LocalDate fiveYearsAgo = LocalDate.now().minusYears(5);
        BookDocument bookDocument = createBookDocument(fiveYearsAgo);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.1);
    }

    @Test
    @DisplayName("출간일이 null인 경우 - 기본값 반환")
    void score_NullPublishedDate_ReturnsDefault() {
        // Given
        BookDocument bookDocument = createBookDocument(null);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("책을 찾을 수 없는 경우 - 기본값 반환")
    void score_BookNotFound_ReturnsDefault() {
        // Given
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("예외 발생 시 - 기본값 반환")
    void score_ExceptionThrown_ReturnsDefault() {
        // Given
        when(bookDocumentRepository.findById(anyString())).thenThrow(new RuntimeException("DB error"));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("미래 출간 예정 도서 - 최고 점수")
    void score_FuturePublishedBook_HighestScore() {
        // Given
        LocalDate futureDate = LocalDate.now().plusMonths(1);
        BookDocument bookDocument = createBookDocument(futureDate);
        when(bookDocumentRepository.findById(anyString())).thenReturn(Optional.of(bookDocument));

        // When
        double score = freshnessScorer.score(userId, candidate);

        // Then
        // 미래 날짜는 음수 daysSincePublished가 되어 1.0보다 클 수 있음
        assertThat(score).isGreaterThan(1.0);
    }

    @Test
    @DisplayName("getName은 FreshnessScorer를 반환")
    void getName_ReturnsFreshnessScorer() {
        // When
        String name = freshnessScorer.getName();

        // Then
        assertThat(name).isEqualTo("FreshnessScorer");
    }

    @Test
    @DisplayName("getDefaultWeight는 0.05를 반환")
    void getDefaultWeight_Returns005() {
        // When
        double weight = freshnessScorer.getDefaultWeight();

        // Then
        assertThat(weight).isEqualTo(0.05);
    }

    private BookDocument createBookDocument(LocalDate publishedDate) {
        BookDocument document = new BookDocument();
        document.setId(String.valueOf(candidate.getBookId()));
        document.setPublishedDate(publishedDate);
        return document;
    }
}
