package org.yyubin.application.recommendation.port.out;

public interface EmbeddingPort {

    float[] embed(String text);
    int getDimension();
}
