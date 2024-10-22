package study.moum.moum.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.auth.domain.repository.MemberRepository;
import study.moum.auth.dto.MemberDto;
import study.moum.global.error.ErrorCode;
import study.moum.global.error.exception.CustomException;
import study.moum.global.error.exception.NeedLoginException;
import study.moum.moum.team.domain.TeamEntity;
import study.moum.moum.team.domain.TeamMemberEntity;
import study.moum.moum.team.domain.TeamMemberRepository;
import study.moum.moum.team.domain.TeamRepository;
import study.moum.moum.team.dto.TeamDto;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public TeamDto.Response getTeamById(String username, int teamId){

       findLoginUser(username);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(()-> new CustomException(ErrorCode.ILLEGAL_ARGUMENT));

        return new TeamDto.Response(team);
    }

    @Transactional
    public TeamDto.Response createTeam(TeamDto.Request teamRequestDto, String username){

        MemberEntity loginUser = findLoginUser(username);

        TeamDto.Request request = TeamDto.Request.builder()
                .members(new ArrayList<>())
                .teamname(teamRequestDto.getTeamname())
                .description(teamRequestDto.getDescription())
                .leaderId(loginUser.getId())
                .build();

        TeamEntity newTeam = request.toEntity();
        teamRepository.save(newTeam);

        return new TeamDto.Response(newTeam);
    }


    @Transactional
    public MemberDto.Response inviteMember(int teamId, int targetMemberId, String username) {

       MemberEntity loginUser = findLoginUser(username);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT));

        // 팀의 리더인지 확인
        if (team.getLeaderId() != loginUser.getId()) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        // 멤버 찾기
        MemberEntity targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_EXIST));

        // 이미 팀 멤버인지 확인
        boolean isAlreadyMember = teamMemberRepository.existsByTeamAndMember(team, targetMember);
        if (isAlreadyMember) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_INVITED);
        }


        // 팀 멤버 초대 로직
        TeamMemberEntity teamMember = TeamMemberEntity.builder()
                .member(targetMember) // MemberEntity 설정
                .team(team) // TeamEntity 설정
                .build();

        // 팀 멤버 저장
        teamMemberRepository.save(teamMember);

        return new MemberDto.Response(targetMember); // 팀 정보 반환
    }



    /**
     * 팀 해체 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 팀 생성 요청 DTO
     *
     */
    @DeleteMapping("/api/teams/{teamId}")
    public void 팀해체(){
    }

    /**
     * 유저로부터 온 초대 요청 수락 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param
     *
     */
    @PostMapping("/api/teams/{teamId}/accept/{memberId}")
    public void 초대요청수락(){
    }

    /**
     * 유저로부터 온 초대 요청 거절 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param
     *
     */
    @PostMapping("/api/teams/{teamId}/reject/{memberId}")
    public void 초대요청거절(){
    }

    /**
     * 팀에서 멤버 강퇴 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param
     *
     */
    @DeleteMapping("/api/teams/kick/{memberId}")
    public void 멤버강퇴(){
    }

    /**
     * 팀에서 탈퇴 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param
     *
     */
    @DeleteMapping("/api/teams/leave/{memberId}")
    public void 팀탈퇴(){
    }



    public MemberEntity findLoginUser(String username){
        MemberEntity loginUser = memberRepository.findByUsername(username);
        if(loginUser == null){ // loginuser -> leader
            throw new NeedLoginException();
        }

        return loginUser;
    }

}
