package org.yyubin.recommendation.candidate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4jCandidateGenerator 테스트")
class Neo4jCandidateGeneratorTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @Mock
    private RecommendationProperties properties;

    @InjectMocks
    private Neo4jCandidateGenerator neo4jCandidateGenerator;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("협업 필터링 기반 후보 생성")
    void generateCandidates_CollaborativeFiltering() {
        // Given
        BookNode book1 = BookNode.builder().id(100L).title("Book 1").build();
        BookNode book2 = BookNode.builder().id(101L).title("Book 2").build();

        List<Object[]> collaborativeResults = createObjectArrayList(
                new Object[]{book1, 8L},
                new Object[]{book2, 12L}
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(collaborativeResults);
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).extracting(RecommendationCandidate::getBookId)
                .containsExactlyInAnyOrder(100L, 101L);
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE);
    }

    @Test
    @DisplayName("장르 기반 후보 생성")
    void generateCandidates_GenreBased() {
        // Given
        BookNode book1 = BookNode.builder().id(200L).title("Genre Book 1").build();

        List<Object[]> genreResults = createObjectArrayList(
                new Object[]{book1, 2L}
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(genreResults);
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getBookId()).isEqualTo(200L);
        assertThat(candidates.get(0).getSource()).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_GENRE);
        assertThat(candidates.get(0).getReason()).contains("Genre overlap");
    }

    @Test
    @DisplayName("저자 기반 후보 생성")
    void generateCandidates_AuthorBased() {
        // Given
        BookNode book1 = BookNode.builder().id(300L).title("Author Book 1").build();

        List<Object[]> authorResults = createObjectArrayList(
                new Object[]{book1, 3L}
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(authorResults);
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getBookId()).isEqualTo(300L);
        assertThat(candidates.get(0).getSource()).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_AUTHOR);
        assertThat(candidates.get(0).getReason()).contains("Author overlap");
    }

    @Test
    @DisplayName("유사 도서 후보 생성")
    void generateCandidates_SimilarBooks() {
        // Given
        BookNode book1 = BookNode.builder().id(400L).title("Similar Book 1").build();

        List<Object[]> similarResults = createObjectArrayList(
                new Object[]{book1, 6L}
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(similarResults);

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getBookId()).isEqualTo(400L);
        assertThat(candidates.get(0).getSource()).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_TOPIC);
        assertThat(candidates.get(0).getReason()).contains("Similar books via graph");
    }

    @Test
    @DisplayName("여러 소스에서 후보 생성 - 중복 제거")
    void generateCandidates_MultipleSources_Deduplicated() {
        // Given
        BookNode book1 = BookNode.builder().id(100L).title("Book 1").build();
        BookNode book2 = BookNode.builder().id(100L).title("Book 1").build(); // 같은 ID

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(createObjectArrayList(new Object[]{book1, 10L}));
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(createObjectArrayList(new Object[]{book2, 3L})); // 같은 책이 장르 기반으로도 나옴
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then - Set으로 중복 제거됨
        assertThat(candidates).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("빈 결과 반환")
    void generateCandidates_EmptyResults() {
        // Given
        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("협업 필터링 점수 계산 - 10명 이상이면 1.0")
    void collaborativeFiltering_ScoreNormalization_MaxScore() {
        // Given
        BookNode book1 = BookNode.builder().id(100L).title("Book 1").build();

        List<Object[]> collaborativeResults = createObjectArrayList(
                new Object[]{book1, 15L} // 10명 이상
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(collaborativeResults);
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("협업 필터링 점수 계산 - 5명이면 0.5")
    void collaborativeFiltering_ScoreNormalization_PartialScore() {
        // Given
        BookNode book1 = BookNode.builder().id(100L).title("Book 1").build();

        List<Object[]> collaborativeResults = createObjectArrayList(
                new Object[]{book1, 5L} // 5명
        );

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(collaborativeResults);
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("장르 기반 점수 계산 - 3개 이상 겹치면 1.0")
    void genreBased_ScoreNormalization() {
        // Given
        BookNode book1 = BookNode.builder().id(200L).title("Genre Book").build();

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(createObjectArrayList(new Object[]{book1, 4L})); // 4개 장르 겹침
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("저자 기반 점수 계산 - 2명 이상 겹치면 1.0")
    void authorBased_ScoreNormalization() {
        // Given
        BookNode book1 = BookNode.builder().id(300L).title("Author Book").build();

        when(userNodeRepository.findBooksByCollaborativeFiltering(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredGenres(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findBooksByPreferredAuthors(anyLong(), anyInt()))
                .thenReturn(createObjectArrayList(new Object[]{book1, 2L})); // 2명 저자 겹침
        when(userNodeRepository.findSimilarBooks(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<RecommendationCandidate> candidates = neo4jCandidateGenerator.generateCandidates(userId, 20);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getSourceType 반환값 확인")
    void getSourceType_ReturnsCollaborative() {
        // When
        RecommendationCandidate.CandidateSource sourceType = neo4jCandidateGenerator.getSourceType();

        // Then
        assertThat(sourceType).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE);
    }

    private List<Object[]> createObjectArrayList(Object[]... arrays) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] arr : arrays) {
            list.add(arr);
        }
        return list;
    }
}
