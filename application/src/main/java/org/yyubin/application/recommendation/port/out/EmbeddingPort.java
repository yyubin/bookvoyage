package org.yyubin.application.recommendation.port.out;

public interface EmbeddingPort {

    /**
     * 텍스트 임베딩 생성
     *
     * @param text 텍스트
     * @return 벡터 (float 배열)
     */
    float[] embed(String text);

    /**
     * 임베딩 차원 수 반환
     *
     * @return 벡터 차원 (예: 1536 for text-embedding-3-small)
     */
    int getDimension();
}
