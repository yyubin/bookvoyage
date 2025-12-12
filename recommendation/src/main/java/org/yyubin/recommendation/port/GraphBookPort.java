package org.yyubin.recommendation.port;

import java.util.List;
import org.yyubin.recommendation.graph.node.BookNode;

public interface GraphBookPort {
    <S extends BookNode> List<S> saveAll(Iterable<S> books);
}
