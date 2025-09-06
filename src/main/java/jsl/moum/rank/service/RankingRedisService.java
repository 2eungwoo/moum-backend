package jsl.moum.rank.service;

import jsl.moum.batch.RankingSyncBatchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public Set<ZSetOperations.TypedTuple<String>> getTopRankersWithScores(int topN) {
        try {
            return redisTemplate.opsForZSet().reverseRangeWithScores(RankingSyncBatchConfig.RANKING_KEY, 0, topN - 1);
        } catch (DataAccessException e) {
            log.error("Redis getTopRankersWithScores 실패. Fallback을 시도합니다.", e);
            return null; // 실패 시 null 반환하여 Fallback 트리거
        }
    }

    public Long getRankForMember(Integer memberId) {
        try {
            return redisTemplate.opsForZSet().reverseRank(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId));
        } catch (DataAccessException e) {
            log.error("Redis getRankForMember 실패. Fallback을 시도합니다.", e);
            return null; // 실패 시 null 반환하여 Fallback 트리거
        }
    }

    public void incrementMemberScore(Integer memberId, int scoreToAdd) {
        try {
            redisTemplate.opsForZSet().incrementScore(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId), scoreToAdd);
        } catch (DataAccessException e) {
            // 쓰기 실패 시에는 에러 로그만 남기고, DB에만 반영되도록 예외를 전파하지 않음
            log.error("Redis incrementMemberScore 실패. DB에는 반영되지만 Redis 랭킹은 다음 배치 실행 시 동기화됩니다. memberId: {}", memberId, e);
        }
    }
}