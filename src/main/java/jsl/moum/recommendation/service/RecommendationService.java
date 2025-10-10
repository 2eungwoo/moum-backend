package jsl.moum.recommendation.service;

import jsl.moum.community.article.domain.article.ArticleEntity;
import jsl.moum.community.article.dto.ArticleDto;
import jsl.moum.community.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ArticleService articleService;
    private static final String RECOMMENDATION_KEY_PREFIX = "user:%d:recommendations";
    private static final int FALLBACK_RECOMMENDATION_SIZE = 10;


    public List<ArticleDto.Response> getRecommendations(Long userId) {
        try {
            String key = String.format(RECOMMENDATION_KEY_PREFIX, userId);
            // 1. Redis에서 추천 콘텐츠 ID 목록 조회
            List<String> recommendedArticleIdsStr = redisTemplate.opsForList().range(key, 0, -1);

            if (CollectionUtils.isEmpty(recommendedArticleIdsStr)) {
                return getFallbackRecommendations();
            }

            List<Long> recommendedArticleIds = recommendedArticleIdsStr.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // 2. ID 목록으로 콘텐츠 상세 정보 조회
            Map<Long, ArticleDto.Response> articlesMap = articleService.getArticlesByIds(recommendedArticleIds).stream()
                    .collect(Collectors.toMap(ArticleDto.Response::getId, Function.identity()));

            // 3. Redis에서 가져온 추천 순서대로 정렬
            return recommendedArticleIds.stream()
                    .map(articlesMap::get)
                    .collect(Collectors.toList());
        } catch (RedisConnectionFailureException e) {
            // Redis 연결 실패 시 Fallback 로직 실행
            log.error("redis 문제 발생, fallback 실행",e);
            return getFallbackRecommendations();
        }
    }

    private List<ArticleDto.Response> getFallbackRecommendations() {
        // 최신 게시글 10개 DB 조회
        return articleService.getArticleList(0, FALLBACK_RECOMMENDATION_SIZE);
    }
}
