package jsl.moum.batch;

import jakarta.persistence.EntityManagerFactory;
import jsl.moum.auth.domain.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RankingSyncBatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final JobCompletionNotificationListener listener;

    private static final int CHUNK_SIZE = 100;
    public static final String RANKING_KEY = "ranking:exp";

    @Bean
    public Job rankingSyncJob(JobRepository jobRepository, Step rankingSyncStep) {
        return new JobBuilder("rankingSyncJob", jobRepository)
                .start(rankingSyncStep)
                .listener(listener)
                .build();
    }

    @Bean
    public Step rankingSyncStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("rankingSyncStep", jobRepository)
                .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(rankingItemReader(null)) // @StepScope will inject actual value
                .writer(rankingItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<MemberEntity> rankingItemReader(
            @Value("#{jobParameters['lastBatchRunTime']}") LocalDateTime lastBatchRunTime) {
        return new JpaPagingItemReaderBuilder<MemberEntity>()
                .name("rankingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT m FROM MemberEntity m WHERE m.expUpdatedAt > :lastBatchRunTime ORDER BY m.id ASC")
                .parameterValues(Map.of("lastBatchRunTime", lastBatchRunTime))
                .build();
    }

    @Bean
    public ItemWriter<MemberEntity> rankingItemWriter() {
        return items -> {
            items.forEach(item -> {
                if (item.getExp() != null) {
                    redisTemplate.opsForZSet().add(RANKING_KEY, String.valueOf(item.getId()), item.getExp().doubleValue());
                }
            });
        };
    }
}
