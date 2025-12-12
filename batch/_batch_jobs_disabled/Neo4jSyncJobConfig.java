package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.batch.sync.UserSyncDto;
import org.yyubin.recommendation.graph.node.UserNode;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;

import java.util.Map;

/**
 * RDB → Neo4j 동기화 배치 Job
 * - User, Book, 관계 데이터를 Neo4j로 동기화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Neo4jSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final UserNodeRepository userNodeRepository;

    /**
     * Neo4j 동기화 Job
     */
    @Bean
    public Job neo4jSyncJob(Step syncUsersStep) {
        return new JobBuilder("neo4jSyncJob", jobRepository)
                .start(syncUsersStep)
                // 향후 syncBooksStep, syncRelationshipsStep 추가
                .build();
    }

    /**
     * User 동기화 Step
     */
    @Bean
    public Step syncUsersStep(
            ItemReader<UserSyncDto> userReader,
            ItemProcessor<UserSyncDto, UserNode> userProcessor,
            ItemWriter<UserNode> userWriter) {

        int chunkSize = batchProperties.getSync().getNeo4j().getChunkSize();

        return new StepBuilder("syncUsersStep", jobRepository)
                .<UserSyncDto, UserNode>chunk(chunkSize, transactionManager)
                .reader(userReader)
                .processor(userProcessor)
                .writer(userWriter)
                .build();
    }

    /**
     * User Reader (RDB에서 읽기)
     * - 실제 구현 시 User 엔티티 Repository 사용
     */
    @Bean
    public ItemReader<UserSyncDto> userReader() {
        // TODO: 실제 User Repository로 교체 필요
        // 현재는 더미 구현
        return new ItemReader<UserSyncDto>() {
            private boolean read = false;

            @Override
            public UserSyncDto read() {
                if (!read) {
                    read = true;
                    log.info("Reading users from RDB (dummy implementation)");
                    return null; // 더미 데이터
                }
                return null;
            }
        };
    }

    /**
     * User Processor (UserSyncDto → UserNode 변환)
     */
    @Bean
    public ItemProcessor<UserSyncDto, UserNode> userProcessor() {
        return userDto -> {
            if (userDto == null) {
                return null;
            }

            return UserNode.builder()
                    .id(userDto.getId())
                    .username(userDto.getUsername())
                    .email(userDto.getEmail())
                    .createdAt(userDto.getCreatedAt())
                    .build();
        };
    }

    /**
     * User Writer (Neo4j에 저장)
     */
    @Bean
    public ItemWriter<UserNode> userWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }

            log.info("Writing {} users to Neo4j", items.size());
            userNodeRepository.saveAll(items);
            log.info("Successfully wrote {} users", items.size());
        };
    }
}
