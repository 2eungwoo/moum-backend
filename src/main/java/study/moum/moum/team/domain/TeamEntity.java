package study.moum.moum.team.domain;

import jakarta.persistence.*;
import lombok.*;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.community.article.domain.article_details.ArticleDetailsEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "team")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "leader_id")
    private int leaderId;

    @Column(name = "team_name")
    private String teamname;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamMemberEntity> members = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void createDate(){
        this.createdAt = LocalDateTime.now();
    }

//    // todo : 의문점있음. 메소드 내의 로직은 필수로 들어가야할까? 레포지토리쪽에서 검증하고 삭제 처리한건데도?
//    public void removeMemberFromTeam(MemberEntity member) {
//        members.remove(member);
////        TeamMemberEntity targetMember = null;
////
////        for (TeamMemberEntity teamMember : members) {
////            if (teamMember.getMember().getId() == member.getId()) {
////                targetMember = teamMember;
////                break;
////            }
////        }
////
////        // 멤버를 찾았다면 삭제
////        if (targetMember != null) {
////            members.remove(targetMember);
////        }
//    }
}