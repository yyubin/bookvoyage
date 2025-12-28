package org.yyubin.application.review.port;

import java.util.List;
import java.util.Map;

public interface ReviewStatisticsPort {
    
    Map<Long, ReviewStatistics> getBatchStatistics(List<Long> reviewIds);
    
    record ReviewStatistics(
            Integer likeCount,
            Integer commentCount,
            Long viewCount
    ) {
    }
}
