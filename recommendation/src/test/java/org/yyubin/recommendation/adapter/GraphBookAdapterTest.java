package org.yyubin.recommendation.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.repository.BookNodeRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphBookAdapter 테스트")
class GraphBookAdapterTest {

    @Mock
    private BookNodeRepository bookNodeRepository;

    @InjectMocks
    private GraphBookAdapter graphBookAdapter;

    @Nested
    @DisplayName("saveAll 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 BookNode를 저장하면 저장된 노드 리스트를 반환한다")
        void saveAll_MultipleNodes_ReturnsSavedList() {
            // Given
            BookNode node1 = BookNode.builder()
                    .id(1L)
                    .title("테스트 도서 1")
                    .isbn("978-1234567890")
                    .description("설명 1")
                    .viewCount(100)
                    .wishlistCount(50)
                    .reviewCount(10)
                    .publishedDate(LocalDate.of(2024, 1, 1))
                    .build();

            BookNode node2 = BookNode.builder()
                    .id(2L)
                    .title("테스트 도서 2")
                    .isbn("978-0987654321")
                    .description("설명 2")
                    .viewCount(200)
                    .wishlistCount(100)
                    .reviewCount(20)
                    .publishedDate(LocalDate.of(2024, 2, 1))
                    .build();

            List<BookNode> nodes = Arrays.asList(node1, node2);

            when(bookNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<BookNode> result = graphBookAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            verify(bookNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("빈 리스트를 저장하면 빈 리스트를 반환한다")
        void saveAll_EmptyList_ReturnsEmptyList() {
            // Given
            List<BookNode> emptyList = Collections.emptyList();
            when(bookNodeRepository.saveAll(emptyList)).thenReturn(emptyList);

            // When
            List<BookNode> result = graphBookAdapter.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
            verify(bookNodeRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("단일 BookNode를 저장하면 저장된 노드를 포함한 리스트를 반환한다")
        void saveAll_SingleNode_ReturnsSingleElementList() {
            // Given
            BookNode node = BookNode.builder()
                    .id(1L)
                    .title("단일 도서")
                    .isbn("978-1111111111")
                    .description("단일 설명")
                    .viewCount(50)
                    .wishlistCount(25)
                    .reviewCount(5)
                    .publishedDate(LocalDate.of(2024, 3, 1))
                    .build();

            List<BookNode> nodes = Collections.singletonList(node);
            when(bookNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<BookNode> result = graphBookAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("단일 도서");
            assertThat(result.get(0).getIsbn()).isEqualTo("978-1111111111");
            verify(bookNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("인기도 지표가 포함된 노드를 저장할 수 있다")
        void saveAll_WithPopularityMetrics_SavesCorrectly() {
            // Given
            BookNode node = BookNode.builder()
                    .id(1L)
                    .title("인기 도서")
                    .viewCount(10000)
                    .wishlistCount(5000)
                    .reviewCount(500)
                    .build();

            List<BookNode> nodes = Collections.singletonList(node);
            when(bookNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<BookNode> result = graphBookAdapter.saveAll(nodes);

            // Then
            assertThat(result.get(0).getViewCount()).isEqualTo(10000);
            assertThat(result.get(0).getWishlistCount()).isEqualTo(5000);
            assertThat(result.get(0).getReviewCount()).isEqualTo(500);
        }
    }
}
