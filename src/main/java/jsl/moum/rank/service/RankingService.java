package jsl.moum.rank.service;

import jsl.moum.auth.domain.CustomUserDetails;
import jsl.moum.auth.domain.entity.MemberEntity;
import jsl.moum.auth.domain.repository.MemberRepository;
import jsl.moum.rank.dto.RankingInfoResponse;
import lombok.RequiredArgsConstructor;
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

    private final MemberRepository memberRepository;
    private final RankingRedisService rankingRedisService;

    @Transactional(readOnly = true)
    public List<RankingInfoResponse> getTopRankings(int topN) {
        if (topN <= 0) {
            topN = 10;
        }
        if (topN > 100) {
            topN = 100;
        }

        Set<ZSetOperations.TypedTuple<String>> typedTuples = rankingRedisService.getTopRankersWithScores(topN);

        if (typedTuples == null || typedTuples.isEmpty()) {
            return List.of();
        }

        List<Integer> memberIds = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<MemberEntity> members = memberRepository.findAllById(memberIds);
        Map<Integer, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getId, member -> member));

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
    public RankingInfoResponse getMyRank(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        Integer memberId = userDetails.getMemberId();

        Long rank = rankingRedisService.getRankForMember(memberId);

        if (rank == null) {
            return null;
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElse(null);
        
        if (member == null) {
            return null;
        }

        return RankingInfoResponse.builder()
                .rank(rank + 1)
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

        rankingRedisService.incrementMemberScore(memberId, expToAdd);
    }
}
