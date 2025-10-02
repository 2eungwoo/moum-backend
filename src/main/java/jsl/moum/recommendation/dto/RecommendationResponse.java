package jsl.moum.recommendation.dto;

import jsl.moum.community.article.domain.Article;
import lombok.Getter;

@Getter
public class RecommendationResponse {
    private Long articleId;
    private String title;
    private String writerNickname;

    public RecommendationResponse(Article article) {
        this.articleId = article.getId();
        this.title = article.getTitle();
        this.writerNickname = article.getMember().getNickname();
    }
}
