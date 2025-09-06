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
        List<RankingInfoResponse> topRankings = rankingService.getTopRankings(topN);
        return ResponseEntity.ok(topRankings);
    }

    @GetMapping("/me")
    public ResponseEntity<RankingInfoResponse> getMyRank(@AuthenticationPrincipal CustomUserDetails userDetails) {
        RankingInfoResponse memberRank = rankingService.getMyRank(userDetails);
        if (memberRank == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberRank);
    }
}