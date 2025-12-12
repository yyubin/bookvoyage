package org.yyubin.recommendation.port;

import java.util.List;
import org.yyubin.recommendation.graph.node.UserNode;

public interface GraphUserPort {
    <S extends UserNode> List<S> saveAll(Iterable<S> users);
}
