package org.yyubin.recommendation.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.graph.node.UserNode;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphUserAdapter 테스트")
class GraphUserAdapterTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @InjectMocks
    private GraphUserAdapter graphUserAdapter;

    @Nested
    @DisplayName("saveAll 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 UserNode를 저장하면 저장된 노드 리스트를 반환한다")
        void saveAll_MultipleNodes_ReturnsSavedList() {
            // Given
            UserNode node1 = UserNode.builder()
                    .id(1L)
                    .username("user1")
                    .email("user1@test.com")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();

            UserNode node2 = UserNode.builder()
                    .id(2L)
                    .username("user2")
                    .email("user2@test.com")
                    .createdAt(LocalDateTime.of(2024, 2, 1, 10, 0))
                    .build();

            List<UserNode> nodes = Arrays.asList(node1, node2);

            when(userNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<UserNode> result = graphUserAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getUsername()).isEqualTo("user1");
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getUsername()).isEqualTo("user2");
            verify(userNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("빈 리스트를 저장하면 빈 리스트를 반환한다")
        void saveAll_EmptyList_ReturnsEmptyList() {
            // Given
            List<UserNode> emptyList = Collections.emptyList();
            when(userNodeRepository.saveAll(emptyList)).thenReturn(emptyList);

            // When
            List<UserNode> result = graphUserAdapter.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
            verify(userNodeRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("단일 UserNode를 저장하면 저장된 노드를 포함한 리스트를 반환한다")
        void saveAll_SingleNode_ReturnsSingleElementList() {
            // Given
            UserNode node = UserNode.builder()
                    .id(1L)
                    .username("singleuser")
                    .email("single@test.com")
                    .createdAt(LocalDateTime.of(2024, 3, 1, 10, 0))
                    .build();

            List<UserNode> nodes = Collections.singletonList(node);
            when(userNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<UserNode> result = graphUserAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("singleuser");
            assertThat(result.get(0).getEmail()).isEqualTo("single@test.com");
            verify(userNodeRepository).saveAll(nodes);
        }

        @Test
        @DisplayName("createdAt이 포함된 노드를 저장할 수 있다")
        void saveAll_WithCreatedAt_SavesCorrectly() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
            UserNode node = UserNode.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .createdAt(createdAt)
                    .build();

            List<UserNode> nodes = Collections.singletonList(node);
            when(userNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<UserNode> result = graphUserAdapter.saveAll(nodes);

            // Then
            assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.get(0).getCreatedAt().getYear()).isEqualTo(2024);
            assertThat(result.get(0).getCreatedAt().getMonthValue()).isEqualTo(6);
        }

        @Test
        @DisplayName("이메일이 없는 노드도 저장할 수 있다")
        void saveAll_WithoutEmail_SavesCorrectly() {
            // Given
            UserNode node = UserNode.builder()
                    .id(1L)
                    .username("noEmailUser")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<UserNode> nodes = Collections.singletonList(node);
            when(userNodeRepository.saveAll(nodes)).thenReturn(nodes);

            // When
            List<UserNode> result = graphUserAdapter.saveAll(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isNull();
            assertThat(result.get(0).getUsername()).isEqualTo("noEmailUser");
        }
    }
}
