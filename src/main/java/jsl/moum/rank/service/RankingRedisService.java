package jsl.moum.rank.service;

import jsl.moum.batch.RankingSyncBatchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public Set<ZSetOperations.TypedTuple<String>> getTopRankersWithScores(int topN) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(RankingSyncBatchConfig.RANKING_KEY, 0, topN - 1);
    }

    public Long getRankForMember(Integer memberId) {
        return redisTemplate.opsForZSet().reverseRank(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId));
    }

    public void incrementMemberScore(Integer memberId, int scoreToAdd) {
        redisTemplate.opsForZSet().incrementScore(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId), scoreToAdd);
    }
}
