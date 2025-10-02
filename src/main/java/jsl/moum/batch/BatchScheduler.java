package jsl.moum.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job rankingSyncJob;
    private final Job recommendationJob;

    // 매 시간 정각에 랭킹 동기화 작업 실행
    @Scheduled(cron = "0 0 * * * ?")
    public void runRankingSyncJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", LocalDateTime.now().toString()) // JobParameter는 LocalDateTime을 직접 지원하지 않음
                .toJobParameters();
        jobLauncher.run(rankingSyncJob, jobParameters);
    }

    // 매일 새벽 4시에 추천 목록 생성 작업 실행
    @Scheduled(cron = "0 0 4 * * ?")
    public void runRecommendationJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", LocalDateTime.now().toString())
                .toJobParameters();
        jobLauncher.run(recommendationJob, jobParameters);
    }
}