package jsl.moum.recommendation.batch;

import jsl.moum.community.article.domain.Article;
import jsl.moum.community.article.domain.ArticleRepository;
import jsl.moum.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.persistence.EntityManagerFactory;
import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int CHUNK_SIZE = 100;
    private static final String RECOMMENDATION_KEY_PREFIX = "user:%d:recommendations";

    @Bean
    public Job recommendationJob() {
        return jobBuilderFactory.get("recommendationJob")
                .start(recommendationStep(null))
                .build();
    }

    @Bean
    @StepScope
    public Step recommendationStep(@Value("#{jobParameters[date]}") String date) {
        return stepBuilderFactory.get("recommendationStep")
                .<Member, AbstractMap.SimpleEntry<String, List<String>>>chunk(CHUNK_SIZE)
                .reader(memberReader())
                .processor(recommendationProcessor())
                .writer(recommendationWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Member> memberReader() {
        return new JpaPagingItemReaderBuilder<Member>()
                .name("memberReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT m FROM Member m")
                .build();
    }

    @Bean
    public ItemProcessor<Member, AbstractMap.SimpleEntry<String, List<String>>> recommendationProcessor() {
        return member -> {
            List<Article> relevantArticles = articleRepository.findRelevantArticles(member.getProfile().getGenres());

            List<String> recommendedArticleIds = relevantArticles.stream()
                    .map(article -> {
                        long score = 0;
                        if (member.getProfile().getGenres().contains(article.getTag())) {
                            score += 10;
                        }
                        // 필요에 따라 다른 가산점 로직 추가 (예: 지역, 아티스트 등)
                        return new AbstractMap.SimpleEntry<>(article, score);
                    })
                    .filter(entry -> entry.getValue() > 0)
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .map(entry -> entry.getKey().getId().toString())
                    .limit(50)
                    .collect(Collectors.toList());

            if (recommendedArticleIds.isEmpty()) {
                return null;
            }

            String key = String.format(RECOMMENDATION_KEY_PREFIX, member.getId());
            return new AbstractMap.SimpleEntry<>(key, recommendedArticleIds);
        };
    }

    @Bean
    public ItemWriter<AbstractMap.SimpleEntry<String, List<String>>> recommendationWriter() {
        return items -> {
            log.info("Writing {} recommendation lists to Redis.", items.size());
            for (AbstractMap.SimpleEntry<String, List<String>> item : items) {
                String key = item.getKey();
                List<String> ids = item.getValue();

                // 기존 추천 목록 삭제 후 새로 추가
                redisTemplate.delete(key);
                redisTemplate.opsForList().rightPushAll(key, ids);
            }
        };
    }
}
