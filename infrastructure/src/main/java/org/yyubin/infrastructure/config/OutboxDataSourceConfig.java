package org.yyubin.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.yyubin.infrastructure.persistence.outbox",
        entityManagerFactoryRef = "outboxEntityManagerFactory",
        transactionManagerRef = "outboxTransactionManager"
)
public class OutboxDataSourceConfig {

    @Bean
    @ConfigurationProperties("outbox.datasource")
    public DataSourceProperties outboxDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("outbox.datasource.hikari")
    public DataSource outboxDataSource(
            @Qualifier("outboxDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean outboxEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("outboxDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("org.yyubin.infrastructure.persistence.outbox")
                .persistenceUnit("outbox")
                .build();
    }

    @Bean
    public PlatformTransactionManager outboxTransactionManager(
            @Qualifier("outboxEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
