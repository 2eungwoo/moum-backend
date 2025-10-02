package jsl.moum.recommendation.service;

import jsl.moum.community.article.domain.Article;
import jsl.moum.community.article.domain.ArticleRepository;
import jsl.moum.recommendation.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ArticleRepository articleRepository;
    private static final String RECOMMENDATION_KEY_PREFIX = "user:%d:recommendations";

    public List<RecommendationResponse> getRecommendations(Long userId) {
        String key = String.format(RECOMMENDATION_KEY_PREFIX, userId);
        // 1. Redis에서 추천 콘텐츠 ID 목록 조회
        List<String> recommendedArticleIdsStr = redisTemplate.opsForList().range(key, 0, -1);

        if (CollectionUtils.isEmpty(recommendedArticleIdsStr)) {
            return Collections.emptyList();
        }

        List<Long> recommendedArticleIds = recommendedArticleIdsStr.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 2. ID 목록으로 콘텐츠 상세 정보 조회 (DB)
        Map<Long, Article> articlesMap = articleRepository.findAllById(recommendedArticleIds).stream()
                .collect(Collectors.toMap(Article::getId, Function.identity()));

        // 3. Redis에서 가져온 추천 순서대로 정렬하여 DTO로 변환
        return recommendedArticleIds.stream()
                .map(articlesMap::get)
                .collect(Collectors.toList());
    }
}
