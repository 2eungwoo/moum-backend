package study.moum.auth.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import study.moum.community.article.domain.article.ArticleEntity;
import study.moum.community.likes.domain.LikesEntity;
import study.moum.moum.team.domain.TeamMemberEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "member")
public class MemberEntity { // todo : userdetails implement 여기다가 + db column 에 안넣게하기

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty
    @NotNull
    @Size(min = 3, max = 10)
    @Column(name = "user_name", nullable = false)
    private String username;

    @NotEmpty
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotEmpty
    @NotNull
    @Pattern(regexp = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    @Column(name = "email", nullable = false)
    private String email;

    @OneToMany(mappedBy = "member")
    private List<TeamMemberEntity> teams = new ArrayList<>();

    // role은 회원가입 시 입력하게 할지?
    // admin, 일반사용자, 일반사용자중에서도 연주자,참여자 뭐 이런거 등등..
    @Column(name = "role", nullable = false)
    private String role;
}
