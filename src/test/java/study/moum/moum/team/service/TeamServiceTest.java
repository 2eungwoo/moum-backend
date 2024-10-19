package study.moum.moum.team.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamService teamService;

    private MemberEntity leader;
    private TeamEntity team;
    private TeamDto.Request teamRequestDto;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this);

        leader = MemberEntity.builder()
                .id(1)
                .username("leader")
                .build();

        team = TeamEntity.builder()
                .id(1)
                .leaderId(leader.getId())
                .teamname("Test Team")
                .build();

        teamRequestDto = TeamDto.Request.builder()
                .teamname("Test Team")
                .description("Team Description")
                .leaderId(leader.getId())
                .build();
    }

    @Test
    @DisplayName("팀 생성 성공")
    void create_team_success() {
        // given

        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(leader);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(team);

        TeamDto.Response response = teamService.createTeam(teamRequestDto, leader.getUsername());

        // then
        assertEquals("Test Team", response.getTeamName());
        assertEquals(leader.getId(), response.getLeaderId());
        verify(teamRepository).save(any(TeamEntity.class));
    }


    @Test
    @DisplayName("팀 생성 실패 - 로그인 필요")
    void create_team_fail_needlogin() {
        // given

        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(null); // 로그인 실패 시뮬레이션
//        when(teamRepository.findById(anyInt())).thenReturn(Optional.empty()); // 필요없는 메소드 콜

        // then
        assertThrows(NeedLoginException.class, () -> {
            teamService.createTeam(teamRequestDto, leader.getUsername());
        });

        verify(teamRepository, times(0)).save(any(TeamEntity.class));
    }

    @Test
    @DisplayName("팀 삭제 성공")
    void delete_team_success() throws Exception{

    }

    @Test
    @DisplayName("팀 삭제 실패 - 리더가 아님")
    void delete_team_fail_notleader() throws Exception{

    }

    @Test
    @DisplayName("팀 삭제 실패 - 없는 팀")
    void delete_team_fail_noteam() throws Exception{

    }

    @Test
    @DisplayName("팀 멤버 초대 성공")
    void invite_member_success() {
        // given
        when(memberRepository.findByUsername(anyString())).thenReturn(leader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(team));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.of(leader));
        when(teamMemberRepository.save(any(TeamMemberEntity.class))).thenReturn(new TeamMemberEntity());

        // when
        MemberDto.Response response = teamService.inviteMember(1, leader.getId(), leader.getUsername());

        // then
        assertEquals(leader.getId(), response.getId());
        verify(teamMemberRepository).save(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("팀 멤버 초대 실패 - 리더가 아닌 경우")
    void invite_member_fail_notleader() {
        // given
        MemberEntity notLeader = MemberEntity.builder()
                .id(2)
                .username("notLeader")
                .build();

        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(notLeader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(team));

        // then
        assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, leader.getId(), notLeader.getUsername());
        });

        assertEquals("권한이 없습니다.",ErrorCode.NO_AUTHORITY.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 초대 실패 - 존재하지 않는 팀")
    void invite_member_fail_noteam() {
        // given
        when(memberRepository.findByUsername(anyString())).thenReturn(leader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.empty());

        // then
         assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, leader.getId(), leader.getUsername());
        });

        assertEquals("유효하지 않은 데이터입니다.",ErrorCode.ILLEGAL_ARGUMENT.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 초대 실패 - 존재하지 않는 멤버")
    void invite_member_fail_nomember() {
        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(leader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(team));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.empty());

        // then
        assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, leader.getId(), leader.getUsername());
        });

        assertEquals("존재하지 않은 회원입니다.",ErrorCode.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("초대 실패 - 이미 초대한 회원")
    void inviteMember_Fail_AlreadyInvited() {
        // given
        MemberEntity alreadymember = MemberEntity.builder()
                .id(2)
                .username("alreadyMember")
                .build();

        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(leader);
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(memberRepository.findById(2)).thenReturn(Optional.of(alreadymember));
        when(teamMemberRepository.existsByTeamAndMember(team, alreadymember)).thenReturn(true); // 이미 멤버임

        // then
        assertThrows(CustomException.class,
                () -> teamService.inviteMember(team.getId(), alreadymember.getId(), leader.getUsername()));

        assertEquals("이미 초대된 멤버입니다.",ErrorCode.MEMBER_ALREADY_INVITED.getMessage());
        verify(teamMemberRepository, times(0)).save(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("멤버 추방 성공")
    void exile_member_success() throws Exception {

    }


    @Test
    @DisplayName("멤버 추방 실패 - 리더 아님")
    void exile_member_notleader() throws Exception {

    }


    @Test
    @DisplayName("멤버 추방 실패 - 팀 멤버가 아님")
    void exile_member_fail_notmember() throws Exception {

    }


    @Test
    @DisplayName("팀 정보 수정 성공")
    void update_team_info_success() throws Exception {

    }

    @Test
    @DisplayName("팀 정보 수정 실패 - 리더가 아님")
    void update_team_ino_fail_notleader() throws Exception {

    }



}