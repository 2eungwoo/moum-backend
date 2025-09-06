package jsl.moum.rank.service;

import jsl.moum.auth.domain.CustomUserDetails;
import jsl.moum.auth.domain.entity.MemberEntity;
import jsl.moum.auth.domain.repository.MemberRepository;
import jsl.moum.rank.dto.RankingInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
@Slf4j
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
            // Redis 장애 발생 또는 데이터 없음. RDB Fallback
            log.warn("Redis 랭킹 조회 실패 또는 데이터 없음. RDB에서 Fallback 조회 시도.");
            return getTopRankingsFromRdbFallback(topN);
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

    private List<RankingInfoResponse> getTopRankingsFromRdbFallback(int topN) {
        List<MemberEntity> members = memberRepository.findByOrderByExpDesc(PageRequest.of(0, topN)).getContent();
        List<RankingInfoResponse> responseList = new ArrayList<>();
        long rank = 1;
        for (MemberEntity member : members) {
            responseList.add(RankingInfoResponse.builder()
                    .rank(rank++)
                    .memberId(member.getId())
                    .username(member.getUsername())
                    .exp(member.getExp())
                    .tier(member.getTier())
                    .profileImageUrl(member.getProfileImageUrl())
                    .build());
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
            // Redis 장애 발생 또는 데이터 없음. RDB Fallback
            log.warn("Redis 개인 랭킹 조회 실패 또는 데이터 없음. RDB에서 Fallback 조회 시도.");
            return getMyRankFromRdbFallback(memberId);
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

    private RankingInfoResponse getMyRankFromRdbFallback(Integer memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElse(null);

        if (member == null) {
            return null;
        }

        long rank = memberRepository.findRankByExp(member.getExp());

        return RankingInfoResponse.builder()
                .rank(rank)
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