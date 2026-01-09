package org.yyubin.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "org.yyubin")
@ConfigurationPropertiesScan(basePackages = "org.yyubin")
@EnableJpaRepositories(basePackages = "org.yyubin.infrastructure.persistence")
@EntityScan(basePackages = "org.yyubin.infrastructure.persistence")
public class ApiApplication {

    public static void main(String[] args) {
        // Load .env from current or parent directories (IDE runs often set cwd to api/)
        Dotenv dotenv = Dotenv.configure()
                .directory(findEnvDirectory())
                .ignoreIfMissing()
                .load();

        // Set environment variables as system properties
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(ApiApplication.class, args);
    }

    private static String findEnvDirectory() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (current.resolve(".env").toFile().exists()) {
                return current.toString();
            }
            current = current.getParent();
        }
        return "./";
    }
}
