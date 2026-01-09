package org.yyubin.batch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "batch")
public class BatchProperties {

    private final Sync sync = new Sync();
    private final Recommendation recommendation = new Recommendation();
    private final Schedule schedule = new Schedule();

    @Getter
    @Setter
    public static class Sync {
        private final Neo4j neo4j = new Neo4j();
        private final Elasticsearch elasticsearch = new Elasticsearch();

        @Getter
        @Setter
        public static class Neo4j {
            private int chunkSize = 100;
            private int pageSize = 1000;
        }

        @Getter
        @Setter
        public static class Elasticsearch {
            private int chunkSize = 100;
            private int pageSize = 1000;
        }
    }

    @Getter
    @Setter
    public static class Recommendation {
        private int chunkSize = 50;
        private int maxCandidates = 500;
    }

    @Getter
    @Setter
    public static class Schedule {
        private String neo4j = "0 */10 * * * *";
        private String elasticsearch = "0 */30 * * * *";
        private String recommendation = "0 0 * * * *";
        private String viewFlush = "0 */15 * * * *";
        private String communityTrend = "0 0 * * * *";
    }
}
