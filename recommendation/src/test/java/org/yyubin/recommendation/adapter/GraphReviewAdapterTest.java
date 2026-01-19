package org.yyubin.recommendation.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.recommendation.review.graph.ReviewNodeRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphReviewAdapter 테스트")
class GraphReviewAdapterTest {

    @Mock
    private ReviewNodeRepository reviewNodeRepository;

    @InjectMocks
    private GraphReviewAdapter graphReviewAdapter;

    @Nested
    @DisplayName("saveAll 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 ReviewNode를 저장하면 저장된 노드 리스트를 반환한다")
        void saveAll_MultipleNodes_ReturnsSavedList() {
            // Given
            ReviewNode node1 = new ReviewNode(1L, 100L, 200L, null);
            ReviewNode node2 = new ReviewNode(2L, 101L, 201L, null);

            List<ReviewNode> nodes = Arrays.asList(node1, node2);

            when(reviewNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<ReviewNode> result = graphReviewAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getUserId()).isEqualTo(100L);
            assertThat(result.get(0).getBookId()).isEqualTo(200L);
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getUserId()).isEqualTo(101L);
            assertThat(result.get(1).getBookId()).isEqualTo(201L);
            verify(reviewNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("빈 리스트를 저장하면 빈 리스트를 반환한다")
        void saveAll_EmptyList_ReturnsEmptyList() {
            // Given
            List<ReviewNode> emptyList = Collections.emptyList();
            when(reviewNodeRepository.saveAll(emptyList)).thenReturn(emptyList);

            // When
            List<ReviewNode> result = graphReviewAdapter.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
            verify(reviewNodeRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("단일 ReviewNode를 저장하면 저장된 노드를 포함한 리스트를 반환한다")
        void saveAll_SingleNode_ReturnsSingleElementList() {
            // Given
            ReviewNode node = new ReviewNode(1L, 100L, 200L, null);

            List<ReviewNode> nodes = Collections.singletonList(node);
            when(reviewNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<ReviewNode> result = graphReviewAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getUserId()).isEqualTo(100L);
            assertThat(result.get(0).getBookId()).isEqualTo(200L);
            verify(reviewNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("동일한 사용자의 여러 리뷰를 저장할 수 있다")
        void saveAll_SameUserMultipleReviews_SavesCorrectly() {
            // Given
            Long userId = 100L;
            ReviewNode node1 = new ReviewNode(1L, userId, 200L, null);
            ReviewNode node2 = new ReviewNode(2L, userId, 201L, null);
            ReviewNode node3 = new ReviewNode(3L, userId, 202L, null);

            List<ReviewNode> nodes = Arrays.asList(node1, node2, node3);
            when(reviewNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<ReviewNode> result = graphReviewAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(node -> node.getUserId().equals(userId));
            assertThat(result.stream().map(ReviewNode::getBookId))
                    .containsExactly(200L, 201L, 202L);
        }

        @Test
        @DisplayName("동일한 도서에 대한 여러 리뷰를 저장할 수 있다")
        void saveAll_SameBookMultipleReviews_SavesCorrectly() {
            // Given
            Long bookId = 200L;
            ReviewNode node1 = new ReviewNode(1L, 100L, bookId, null);
            ReviewNode node2 = new ReviewNode(2L, 101L, bookId, null);

            List<ReviewNode> nodes = Arrays.asList(node1, node2);
            when(reviewNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<ReviewNode> result = graphReviewAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(node -> node.getBookId().equals(bookId));
            assertThat(result.stream().map(ReviewNode::getUserId))
                    .containsExactly(100L, 101L);
        }
    }
}
