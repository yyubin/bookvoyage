package org.yyubin.application.recommendation.port.out;

public interface LLMPort {

    /**
     * LLM 완성 요청
     *
     * @param prompt 프롬프트
     * @return LLM 응답
     */
    String complete(String prompt);

    /**
     * LLM 완성 요청 (토큰 수 제한)
     *
     * @param prompt 프롬프트
     * @param maxTokens 최대 토큰 수
     * @return LLM 응답
     */
    String complete(String prompt, int maxTokens);
}
