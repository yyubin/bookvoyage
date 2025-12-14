package org.yyubin.batch.job;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.batch.service.BatchBookSyncService;
import org.yyubin.batch.service.BatchUserSyncService;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.batch.sync.UserSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;
import org.yyubin.recommendation.graph.node.AuthorNode;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.node.GenreNode;
import org.yyubin.recommendation.graph.node.LikedReviewOfRelationship;
import org.yyubin.recommendation.graph.node.TopicNode;
import org.yyubin.recommendation.graph.node.UserNode;
import org.yyubin.recommendation.graph.node.ViewedRelationship;
import org.yyubin.recommendation.graph.node.WishlistedRelationship;
import org.yyubin.recommendation.graph.repository.BookNodeRepository;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;
import org.yyubin.recommendation.port.GraphBookPort;
import org.yyubin.recommendation.port.GraphUserPort;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Neo4jSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final GraphUserPort graphUserPort;
    private final GraphBookPort graphBookPort;
    private final BatchUserSyncService batchUserSyncService;
    private final BatchBookSyncService batchBookSyncService;

    @Bean
    public Job neo4jSyncJob(Step syncBooksToNeo4jStep, Step syncUsersToNeo4jStep) {
        return new JobBuilder("neo4jSyncJob", jobRepository)
                .start(syncBooksToNeo4jStep)
                .next(syncUsersToNeo4jStep)
                .build();
    }

    @Bean
    public Step syncBooksToNeo4jStep(
            ItemReader<BookEntity> bookReaderForNeo4j,
            ItemProcessor<BookEntity, BookNode> bookNodeProcessor,
            ItemWriter<BookNode> bookNodeWriter
    ) {
        int chunkSize = batchProperties.getSync().getNeo4j().getChunkSize();
        return new StepBuilder("syncBooksToNeo4jStep", jobRepository)
                .<BookEntity, BookNode>chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(bookReaderForNeo4j)
                .processor(bookNodeProcessor)
                .writer(bookNodeWriter)
                .build();
    }

    @Bean
    public Step syncUsersToNeo4jStep(
            ItemReader<UserEntity> userReaderForNeo4j,
            ItemProcessor<UserEntity, UserNode> userNodeProcessor,
            ItemWriter<UserNode> userNodeWriter
    ) {
        int chunkSize = batchProperties.getSync().getNeo4j().getChunkSize();

        return new StepBuilder("syncUsersToNeo4jStep", jobRepository)
                .<UserEntity, UserNode>chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(userReaderForNeo4j)
                .processor(userNodeProcessor)
                .writer(userNodeWriter)
                .build();
    }


    @Bean
    public RepositoryItemReader<BookEntity> bookReaderForNeo4j() {
        return new RepositoryItemReaderBuilder<BookEntity>()
                .name("bookReaderForNeo4j")
                .repository(bookJpaRepository)
                .methodName("findAll")
                .pageSize(batchProperties.getSync().getNeo4j().getPageSize())
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public RepositoryItemReader<UserEntity> userReaderForNeo4j() {
        return new RepositoryItemReaderBuilder<UserEntity>()
                .name("userReaderForNeo4j")
                .repository(userJpaRepository)
                .methodName("findAll")
                .pageSize(batchProperties.getSync().getNeo4j().getPageSize())
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BookEntity, BookNode> bookNodeProcessor() {
        return entity -> {
            BookSyncDto dto = batchBookSyncService.buildSyncData(entity);

            Set<AuthorNode> authors = dto.authors().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(name -> AuthorNode.builder()
                            .id(generateAuthorId(name))
                            .name(name)
                            .build())
                    .collect(Collectors.toSet());

            Set<GenreNode> genres = dto.genres().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(name -> GenreNode.builder()
                            .name(name)
                            .bookCount(dto.reviewCount())
                            .build())
                    .collect(Collectors.toSet());

            Set<TopicNode> topics = dto.topics().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(name -> TopicNode.builder()
                            .name(name)
                            .bookCount(dto.reviewCount())
                            .build())
                    .collect(Collectors.toSet());

            return BookNode.builder()
                    .id(dto.id())
                    .title(dto.title())
                    .isbn(dto.isbn())
                    .description(dto.description())
                    .publishedDate(dto.publishedDate())
                    .viewCount(dto.viewCount())
                    .wishlistCount(dto.wishlistCount())
                    .reviewCount(dto.reviewCount())
                    .authors(authors)
                    .genres(genres)
                    .topics(topics)
                    .build();
        };
    }

    @Bean
    public ItemWriter<BookNode> bookNodeWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Saving {} books to Neo4j", items.size());
            graphBookPort.saveAll(items);
        };
    }

    @Bean
    public ItemProcessor<UserEntity, UserNode> userNodeProcessor() {
        return entity -> {
            UserSyncDto dto = batchUserSyncService.buildSyncData(entity);

            UserNode userNode = UserNode.builder()
                    .id(dto.id())
                    .username(dto.username())
                    .email(dto.email())
                    .createdAt(dto.createdAt())
                    .build();

            dto.viewedBooks().forEach(view -> userNode.getViewedBooks().add(
                    ViewedRelationship.builder()
                            .book(BookNode.builder().id(view.bookId()).build())
                            .firstViewedAt(view.firstViewedAt())
                            .lastViewedAt(view.lastViewedAt())
                            .viewCount(view.viewCount() > 0 ? view.viewCount() : 1)
                            .build()
            ));

            dto.wishlistedBooks().forEach(wishlist -> userNode.getWishlistedBooks().add(
                    WishlistedRelationship.builder()
                            .book(BookNode.builder().id(wishlist.bookId()).build())
                            .addedAt(wishlist.addedAt())
                            .build()
            ));

            dto.likedReviewBooks().forEach(liked -> userNode.getLikedReviewBooks().add(
                    LikedReviewOfRelationship.builder()
                            .book(BookNode.builder().id(liked.bookId()).build())
                            .reviewId(liked.reviewId())
                            .likedAt(liked.likedAt())
                            .build()
            ));

            return userNode;
        };
    }

    @Bean
    public ItemWriter<UserNode> userNodeWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Saving {} users to Neo4j", items.size());
            graphUserPort.saveAll(items);
        };
    }

    private long generateAuthorId(String name) {
        long hash = Math.abs(name.toLowerCase().hashCode());
        return hash == 0 ? 1 : hash;
    }
}
