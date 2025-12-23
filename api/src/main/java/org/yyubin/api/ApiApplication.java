package org.yyubin.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "org.yyubin")
@ConfigurationPropertiesScan(basePackages = "org.yyubin")
@EnableJpaRepositories(basePackages = "org.yyubin.infrastructure.persistence")
@EntityScan(basePackages = "org.yyubin.infrastructure.persistence")
public class ApiApplication {

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        // Set environment variables as system properties
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(ApiApplication.class, args);
    }

}
