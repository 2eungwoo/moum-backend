package jsl.moum.rank.service;

import jsl.moum.auth.domain.entity.MemberEntity;
import jsl.moum.auth.domain.repository.MemberRepository;
import jsl.moum.batch.RankingSyncBatchConfig;
import jsl.moum.rank.dto.RankingInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<RankingInfoResponse> getTopRankings(int topN) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(RankingSyncBatchConfig.RANKING_KEY, 0, topN - 1);

        if (typedTuples == null || typedTuples.isEmpty()) {
            return List.of();
        }

        List<Integer> memberIds = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Fetch all members in one query
        List<MemberEntity> members = memberRepository.findAllById(memberIds);
        Map<Integer, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getId, member -> member));

        // Build response while preserving Redis order
        long rank = 1;
        List<RankingInfoResponse> responseList = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            Integer memberId = Integer.parseInt(tuple.getValue());
            MemberEntity member = memberMap.get(memberId);
            if (member != null) {
                responseList.add(RankingInfoResponse.builder()
                        .rank(rank++)
                        .memberId(member.getId())
                        .username(member.getUsername())
                        .exp(member.getExp())
                        .tier(member.getTier())
                        .profileImageUrl(member.getProfileImageUrl())
                        .build());
            }
        }
        return responseList;
    }

    @Transactional(readOnly = true)
    public RankingInfoResponse getMemberRank(Integer memberId) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Long rank = zSetOperations.reverseRank(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId));

        if (rank == null) {
            // Not in ranking
            return null;
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElse(null);
        
        if (member == null) {
            return null;
        }

        return RankingInfoResponse.builder()
                .rank(rank + 1) // rank is 0-based
                .memberId(member.getId())
                .username(member.getUsername())
                .exp(member.getExp())
                .tier(member.getTier())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    @Transactional
    public void updateMemberExp(Integer memberId, int expToAdd) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        member.updateMemberExpAndRank(expToAdd);
        // The change to member will be saved by dirty checking at the end of the transaction.

        // Update Redis in real-time
        redisTemplate.opsForZSet().incrementScore(RankingSyncBatchConfig.RANKING_KEY, String.valueOf(memberId), expToAdd);
    }
}
