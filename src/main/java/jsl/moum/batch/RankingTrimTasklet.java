package jsl.moum.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingTrimTasklet implements Tasklet {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long TOP_N = 10000; // 상위 10,000명만 유지

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Executing RankingTrimTasklet: Trimming Redis Sorted Set to TOP {}", TOP_N);

        try {
            // Sorted Set 에서 멤버 수가 TOP_N 초과하면 -> 가장 낮은 점수의 멤버부터 삭제
            // 0 ~ (TOP_N + 1)까지 삭제 => 점수 제일 높은 TOP_N개의 멤버만 남음
            redisTemplate.opsForZSet().removeRange(RankingSyncBatchConfig.RANKING_KEY, 0, -(TOP_N + 1));
            log.info("Successfully trimmed ranking sorted set.");
        } catch (Exception e) {
            log.error("Failed to trim ranking sorted set", e);
        }

        return RepeatStatus.FINISHED;
    }
}
