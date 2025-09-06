package jsl.moum.rank.dto;

import jsl.moum.rank.Rank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingInfoResponse {
    private Long rank;
    private Integer memberId;
    private String username;
    private Integer exp;
    private Rank tier;
    private String profileImageUrl;
}
