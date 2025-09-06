package jsl.moum.rank.controller;

import jsl.moum.auth.domain.CustomUserDetails;
import jsl.moum.rank.dto.RankingInfoResponse;
import jsl.moum.rank.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/top/{topN}")
    public ResponseEntity<List<RankingInfoResponse>> getTopRankings(@PathVariable int topN) {
        if (topN <= 0) {
            topN = 10; // default to top 10
        }
        if (topN > 100) {
            topN = 100; // limit to top 100
        }
        List<RankingInfoResponse> topRankings = rankingService.getTopRankings(topN);
        return ResponseEntity.ok(topRankings);
    }

    @GetMapping("/me")
    public ResponseEntity<RankingInfoResponse> getMyRank(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        Integer memberId = userDetails.getMemberId();
        RankingInfoResponse memberRank = rankingService.getMemberRank(memberId);
        if (memberRank == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberRank);
    }
}
