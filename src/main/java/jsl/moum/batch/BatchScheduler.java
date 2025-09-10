package jsl.moum.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job rankingSyncJob;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 * * * ?")
    public void runRankingSyncJob() throws Exception {
        String lockName = "rankingSyncJobLock"; // 락 이름
        RLock lock = redissonClient.getLock(lockName);

        // 락 획득 시도 (최대 10초 대기, 락 유지 시간 30분)
        // 락 획득에 성공하면 true, 실패하면 false 반환
        boolean locked = lock.tryLock(10, 30, TimeUnit.MINUTES);

        if (locked) {
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLocalDateTime("lastBatchRunTime", LocalDateTime.now())
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();
                jobLauncher.run(rankingSyncJob, jobParameters);
            } finally {
                lock.unlock(); // Job 완료 후 락 해제
            }
        } else {
            // 락 획득 실패 (다른 인스턴스가 이미 Job 실행 중)
            System.out.println("다른 인스턴스에서 랭킹 동기화 Job이 이미 실행 중입니다. 현재 인스턴스는 Job을 실행하지 않습니다.");
        }
    }
}
