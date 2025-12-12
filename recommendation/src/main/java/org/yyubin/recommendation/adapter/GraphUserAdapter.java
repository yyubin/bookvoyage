package org.yyubin.recommendation.adapter;

import java.util.List;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.port.GraphUserPort;
import org.yyubin.recommendation.graph.node.UserNode;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;

/**
 * GraphUserPort 구현체
 * Batch 모듈에서 UserNode를 Neo4j에 저장할 때 사용
 */
@Component
public class GraphUserAdapter implements GraphUserPort {

    private final UserNodeRepository userNodeRepository;

    public GraphUserAdapter(UserNodeRepository userNodeRepository) {
        this.userNodeRepository = userNodeRepository;
    }

    @Override
    public <S extends UserNode> List<S> saveAll(Iterable<S> users) {
        List<S> result = new java.util.ArrayList<>();
        userNodeRepository.saveAll(users).forEach(result::add);
        return result;
    }
}
