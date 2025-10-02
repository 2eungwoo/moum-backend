package jsl.moum.recommendation.controller;

import jsl.moum.auth.domain.CustomUserDetails;
import jsl.moum.recommendation.dto.RecommendationResponse;
import jsl.moum.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/me")
    public ResponseEntity<List<RecommendationResponse>> getMyRecommendations(
            @AuthenticationPrincipal Long userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
}
