package org.yyubin.infrastructure.recommendation.adapter;

import dev.langchain4j.model.chat.ChatLanguageModel;
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
@DisplayName("OpenAIAdapter 테스트")
class OpenAIAdapterTest {

    @Mock
    private ChatLanguageModel chatModel;

    private OpenAIAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = createAdapterWithMockedModel();
    }

    private OpenAIAdapter createAdapterWithMockedModel() {
        OpenAIAdapter adapterInstance = new OpenAIAdapter(
            "test-api-key",
            "gpt-4o-mini",
            500,
            0.7,
            30
        );
        ReflectionTestUtils.setField(adapterInstance, "chatModel", chatModel);
        return adapterInstance;
    }

    @Test
    @DisplayName("complete 호출 시 ChatModel을 사용하여 응답을 생성한다")
    void complete_UsesChatModel() {
        // Given
        String prompt = "Recommend a book about programming";
        String expectedResponse = "I recommend 'Clean Code' by Robert C. Martin";
        when(chatModel.generate(prompt)).thenReturn(expectedResponse);

        // When
        String result = adapter.complete(prompt);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(chatModel).generate(prompt);
    }

    @Test
    @DisplayName("complete with maxTokens 호출 시 ChatModel을 사용한다")
    void complete_WithMaxTokens_UsesChatModel() {
        // Given
        String prompt = "Summarize this book";
        String expectedResponse = "Summary: A great book";
        when(chatModel.generate(prompt)).thenReturn(expectedResponse);

        // When
        String result = adapter.complete(prompt, 100);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(chatModel).generate(prompt);
    }

    @Test
    @DisplayName("ChatModel 예외 발생 시 RuntimeException으로 래핑한다")
    void complete_OnException_ThrowsRuntimeException() {
        // Given
        String prompt = "Test prompt";
        when(chatModel.generate(anyString())).thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThatThrownBy(() -> adapter.complete(prompt))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("LLM API call failed");
    }

    @Test
    @DisplayName("빈 프롬프트도 처리한다")
    void complete_EmptyPrompt_Processes() {
        // Given
        String prompt = "";
        String expectedResponse = "";
        when(chatModel.generate(prompt)).thenReturn(expectedResponse);

        // When
        String result = adapter.complete(prompt);

        // Then
        assertThat(result).isEmpty();
        verify(chatModel).generate(prompt);
    }

    @Test
    @DisplayName("긴 프롬프트도 처리한다")
    void complete_LongPrompt_Processes() {
        // Given
        String prompt = "A".repeat(10000);
        String expectedResponse = "Long response";
        when(chatModel.generate(prompt)).thenReturn(expectedResponse);

        // When
        String result = adapter.complete(prompt);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(chatModel).generate(prompt);
    }
}
