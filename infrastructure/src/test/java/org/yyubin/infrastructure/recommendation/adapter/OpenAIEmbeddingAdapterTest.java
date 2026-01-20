package org.yyubin.infrastructure.recommendation.adapter;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAIEmbeddingAdapter 테스트")
class OpenAIEmbeddingAdapterTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private Response<Embedding> embeddingResponse;

    @Mock
    private Embedding embedding;

    private OpenAIEmbeddingAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = createAdapterWithMockedModel();
    }

    private OpenAIEmbeddingAdapter createAdapterWithMockedModel() {
        OpenAIEmbeddingAdapter adapterInstance = new OpenAIEmbeddingAdapter(
            "test-api-key",
            "text-embedding-3-small",
            1536,
            30
        );
        ReflectionTestUtils.setField(adapterInstance, "embeddingModel", embeddingModel);
        return adapterInstance;
    }

    @Test
    @DisplayName("embed 호출 시 EmbeddingModel을 사용하여 벡터를 생성한다")
    void embed_UsesEmbeddingModel() {
        // Given
        String text = "This is a test sentence";
        float[] expectedVector = new float[]{0.1f, 0.2f, 0.3f};

        when(embeddingModel.embed(text)).thenReturn(embeddingResponse);
        when(embeddingResponse.content()).thenReturn(embedding);
        when(embedding.dimension()).thenReturn(3);
        when(embedding.vector()).thenReturn(expectedVector);

        // When
        float[] result = adapter.embed(text);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo(0.1f);
        assertThat(result[1]).isEqualTo(0.2f);
        assertThat(result[2]).isEqualTo(0.3f);
        verify(embeddingModel).embed(text);
    }

    @Test
    @DisplayName("getDimension은 설정된 차원을 반환한다")
    void getDimension_ReturnsConfiguredDimension() {
        // When
        int dimension = adapter.getDimension();

        // Then
        assertThat(dimension).isEqualTo(1536);
    }

    @Test
    @DisplayName("EmbeddingModel 예외 발생 시 RuntimeException으로 래핑한다")
    void embed_OnException_ThrowsRuntimeException() {
        // Given
        String text = "Test text";
        when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThatThrownBy(() -> adapter.embed(text))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Embedding generation failed");
    }

    @Test
    @DisplayName("빈 텍스트도 임베딩을 생성한다")
    void embed_EmptyText_GeneratesEmbedding() {
        // Given
        String text = "";
        float[] expectedVector = new float[]{0.0f, 0.0f};

        when(embeddingModel.embed(text)).thenReturn(embeddingResponse);
        when(embeddingResponse.content()).thenReturn(embedding);
        when(embedding.dimension()).thenReturn(2);
        when(embedding.vector()).thenReturn(expectedVector);

        // When
        float[] result = adapter.embed(text);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("긴 텍스트도 임베딩을 생성한다")
    void embed_LongText_GeneratesEmbedding() {
        // Given
        String text = "A".repeat(5000);
        float[] expectedVector = new float[]{0.5f};

        when(embeddingModel.embed(text)).thenReturn(embeddingResponse);
        when(embeddingResponse.content()).thenReturn(embedding);
        when(embedding.dimension()).thenReturn(1);
        when(embedding.vector()).thenReturn(expectedVector);

        // When
        float[] result = adapter.embed(text);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(0.5f);
    }
}
