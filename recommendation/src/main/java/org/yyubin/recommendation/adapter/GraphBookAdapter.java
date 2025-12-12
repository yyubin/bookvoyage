package org.yyubin.recommendation.adapter;

import java.util.List;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.port.GraphBookPort;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.repository.BookNodeRepository;

/**
 * GraphBookPort 구현체
 * Batch 모듈에서 BookNode를 Neo4j에 저장할 때 사용
 */
@Component
public class GraphBookAdapter implements GraphBookPort {

    private final BookNodeRepository bookNodeRepository;

    public GraphBookAdapter(BookNodeRepository bookNodeRepository) {
        this.bookNodeRepository = bookNodeRepository;
    }

    @Override
    public <S extends BookNode> List<S> saveAll(Iterable<S> books) {
        List<S> result = new java.util.ArrayList<>();
        bookNodeRepository.saveAll(books).forEach(result::add);
        return result;
    }
}
