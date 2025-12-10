package org.yyubin.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yyubin.domain.review.MentionParser;
import org.yyubin.domain.review.UserFinder;

@Configuration
public class MentionConfig {

    @Bean
    public MentionParser mentionParser(UserFinder userFinder) {
        return new MentionParser(userFinder);
    }
}
