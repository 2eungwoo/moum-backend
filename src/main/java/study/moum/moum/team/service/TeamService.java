package study.moum.moum.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        MemberEntity loginuser = memberRepository.findByUsername(username);
        if(loginuser == null){
            throw new NeedLoginException();
        }

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(()-> new CustomException(ErrorCode.ILLEGAL_ARGUMENT));

        return new TeamDto.Response(team);
    }

    @Transactional
    public TeamDto.Response createTeam(TeamDto.Request teamRequestDto, String username){
        MemberEntity loginuser = memberRepository.findByUsername(username);
        if(loginuser == null){ // loginuser -> leader
            throw new NeedLoginException();
        }

        TeamDto.Request request = TeamDto.Request.builder()
                .members(new ArrayList<>())
                .teamname(teamRequestDto.getTeamname())
                .description(teamRequestDto.getDescription())
                .leaderId(loginuser.getId())
                .build();

        TeamEntity newTeam = request.toEntity();
        teamRepository.save(newTeam);

        return new TeamDto.Response(newTeam);
    }


    @Transactional
    public MemberDto.Response inviteMember(int teamId, int targetMemberId, String username) {

        MemberEntity loginUser = memberRepository.findByUsername(username);
        if (loginUser == null) { // loginuser -> leader
            throw new NeedLoginException();
        }

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


}
