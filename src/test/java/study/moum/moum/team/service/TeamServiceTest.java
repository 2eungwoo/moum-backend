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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Mock
    private TeamMemberRepositoryCustom teamMemberRepositoryCustom;

    @InjectMocks
    private TeamService teamService;

    private MemberEntity mockLeader;
    private MemberEntity mockMember;
    private TeamEntity mockTeam;
    private TeamDto.Request mockTeamRequestDto;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this);

        mockLeader = MemberEntity.builder()
                .id(1)
                .username("leader")
                .build();

        mockMember = MemberEntity.builder()
                .id(2)
                .username("member")
                .build();

        mockTeam = TeamEntity.builder()
                .id(1)
                .leaderId(mockLeader.getId())
                .teamname("Test Team")
                .build();

        mockTeamRequestDto = TeamDto.Request.builder()
                .teamname("Test Team")
                .description("Team Description")
                .leaderId(mockLeader.getId())
                .build();
    }

    @Test
    @DisplayName("팀 생성 성공")
    void create_team_success() {
        // given

        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(mockLeader);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(mockTeam);

        TeamDto.Response response = teamService.createTeam(mockTeamRequestDto, mockLeader.getUsername());

        // then
        assertEquals("Test Team", response.getTeamName());
        assertEquals(mockLeader.getId(), response.getLeaderId());
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
            teamService.createTeam(mockTeamRequestDto, mockLeader.getUsername());
        });

        verify(teamRepository, times(0)).save(any(TeamEntity.class));
    }

    @Test
    @DisplayName("팀 멤버 초대 성공")
    void invite_member_success() {
        // given
        when(memberRepository.findByUsername(anyString())).thenReturn(mockLeader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.of(mockLeader));
        when(teamMemberRepository.save(any(TeamMemberEntity.class))).thenReturn(new TeamMemberEntity());

        // when
        MemberDto.Response response = teamService.inviteMember(1, mockLeader.getId(), mockLeader.getUsername());

        // then
        assertEquals(mockLeader.getId(), response.getId());
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
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(mockTeam));

        // then
        assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, mockLeader.getId(), notLeader.getUsername());
        });

        assertEquals("권한이 없습니다.",ErrorCode.NO_AUTHORITY.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 초대 실패 - 존재하지 않는 팀")
    void invite_member_fail_noteam() {
        // given
        when(memberRepository.findByUsername(anyString())).thenReturn(mockLeader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.empty());

        // then
         assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, mockLeader.getId(), mockLeader.getUsername());
        });

        assertEquals("유효하지 않은 데이터입니다.",ErrorCode.ILLEGAL_ARGUMENT.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 초대 실패 - 존재하지 않는 멤버")
    void invite_member_fail_nomember() {
        // when
        when(memberRepository.findByUsername(anyString())).thenReturn(mockLeader);
        when(teamRepository.findById(anyInt())).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.empty());

        // then
        assertThrows(CustomException.class, () -> {
            teamService.inviteMember(1, mockLeader.getId(), mockLeader.getUsername());
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
        when(memberRepository.findByUsername(anyString())).thenReturn(mockLeader);
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(2)).thenReturn(Optional.of(alreadymember));
        when(teamMemberRepository.existsByTeamAndMember(mockTeam, alreadymember)).thenReturn(true); // 이미 멤버임

        // then
        assertThrows(CustomException.class,
                () -> teamService.inviteMember(mockTeam.getId(), alreadymember.getId(), mockLeader.getUsername()));

        assertEquals("이미 초대된 멤버입니다.",ErrorCode.MEMBER_ALREADY_INVITED.getMessage());
        verify(teamMemberRepository, times(0)).save(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("팀 정보 수정 성공")
    void update_team_info_success() throws Exception {
        // given
        TeamDto.Request teamUpdateRequestDto = TeamDto.Request.builder()
                .teamname("updated team name")
                .description("updated team description")
                .leaderId(mockLeader.getId())
                .build();

        // when
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findByUsername("leader")).thenReturn(mockLeader);

        teamService.updateTeamInfo(mockTeam.getId(), teamUpdateRequestDto, mockLeader.getUsername());

        // then
        // 팀 정보가 업데이트되었는지 확인
        assertThat(mockTeam.getTeamname()).isEqualTo("updated team name");
        assertThat(mockTeam.getDescription()).isEqualTo("updated team description");

        // save() 메소드가 호출되었는지 검증
        verify(teamRepository).save(mockTeam);
    }

    @Test
    @DisplayName("팀 정보 수정 실패 - 리더가 아님")
    void update_team_ino_fail_notleader() throws Exception {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findByUsername(mockMember.getUsername())).thenReturn(mockMember);

        // 팀의 리더가 아닌 경우 예외 발생 확인
        assertThatThrownBy(() -> teamService.updateTeamInfo(1, mockTeamRequestDto, mockMember.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NO_AUTHORITY.getMessage());

        // 팀이 수정되지 않는지 확인
        verify(teamRepository, never()).save(any(TeamEntity.class));
    }

    @Test
    @DisplayName("팀 정보 수정 실패 - 팀 찾을 수 없음")
    void updateTeamInfo_Fail_TeamNotFound() {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // 팀이 존재하지 않는 경우 예외 발생 확인
        assertThatThrownBy(() -> teamService.updateTeamInfo(1, mockTeamRequestDto, mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());

        // 팀이 저장되지 않는지 확인
        verify(teamRepository, never()).save(any(TeamEntity.class));
    }

    @Test
    @DisplayName("팀 해체 성공")
    void deleteTeam_Success() throws Exception {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(1)).thenReturn(Optional.of(mockLeader));

        // when
        teamService.deleteTeamByid(mockTeam.getId(), mockLeader.getUsername());

        // then
        verify(teamRepository).deleteById(mockTeam.getId());
        assertThat(mockTeam.getId()).isEqualTo(null);

    }

    @Test
    @DisplayName("팀 해체 실패 - 리더가 아님")
    void deleteTeam_Fail_NotLeader() throws Exception {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(2)).thenReturn(Optional.of(mockMember));

        // when
        // 리더가 아닐 경우 예외 발생 확인
        assertThatThrownBy(() -> teamService.deleteTeamById(mockTeam.getId(), mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NO_AUTHORITY.getMessage());

        // 삭제가 안되는지 확인
        verify(teamRepository, never()).deleteById(any(Integer.class));
    }


    @Test
    @DisplayName("팀 해체 실패 - 팀 찾을 수 없음")
    void deleteTeam_Fail_TeamNotFound() throws Exception {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // when
        // 팀이 없을 때 예외 발생 확인
        assertThatThrownBy(() -> teamService.deleteTeamById(mockTeam.getId(), mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TEAM_NOT_FOUND.getMessage());

        // 삭제가 안되는지 확인
        verify(teamRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    @DisplayName("팀에서 멤버 강퇴 성공")
    void kickMemberFromTeam_Success() throws Exception {
        // given
        int targetMemberId = mockMember.getId();
        int leaderId = mockLeader.getId();

        // when
        when(teamService.findLoginUser(mockLeader.getUsername())).thenReturn(mockLeader);
        when(teamRepository.findById(1)).thenReturn(Optional.of(mockTeam));
        when(memberRepository.findById(leaderId)).thenReturn(Optional.of(mockLeader));

        teamService.kickMember(targetMemberId, mockLeader.getUsername());

        // then
        assertThat(teamMemberRepository.findById(targetMemberId)).isEmpty();
        verify(teamMemberRepositoryCustom).kickMemberById(any(Integer.class),any(Integer.class));
    }

    @Test
    @DisplayName("팀에서 멤버 강퇴 실패 - 리더가 아님")
    void kickMemberFromTeam_NotLeader() throws Exception {
        // given
        int targetMemberId = 999;
        int mockMemberId = mockMember.getId();
        int leaderId = mockLeader.getId();

        // when
        when(memberRepository.findById(mockMemberId)).thenReturn(Optional.of(mockMember));

        // then
        assertThatThrownBy(() -> teamService.kickMember(targetMemberId, mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NO_AUTHORITY.getMessage());

        verify(teamMemberRepositoryCustom, never()).kickMemberById(any(Integer.class),any(Integer.class));

    }

    @Test
    @DisplayName("팀에서 멤버 강퇴 실패 - 팀 멤버가 아님")
    void kickMemberFromTeam_Fail_NotTeamMember() throws Exception {
        // given
        MemberEntity notMockMember = MemberEntity.builder()
                .id(456)
                .username("not team member")
                .build();

        int mockMemberId = mockMember.getId();
        int leaderId = mockLeader.getId();
        int teamId = mockTeam.getId();

        // when
        when(memberRepository.findById(leaderId)).thenReturn(Optional.of(mockLeader));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepositoryCustom.findMemberInTeamById(456)).thenReturn(Optional.of(notMockMember));

        // then
        assertThatThrownBy(() -> teamService.kickMember(targetMemberId, mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NOT_TEAM_MEMBER.getMessage());

        verify(teamMemberRepositoryCustom, never()).kickMemberById(any(Integer.class),any(Integer.class));

    }

    @Test
    @DisplayName("팀에서 멤버 강퇴 실패 - 팀 찾을 수 없음")
    void kickMemberFromTeam_Fail_TeamNotFound() throws Exception {
        // given
        int targetMemberId = mockMember.getId();
        int leaderId = mockLeader.getId();
        int teamId = mockTeam.getId();

        // when
        when(memberRepository.findById(leaderId)).thenReturn(Optional.of(mockLeader));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> teamService.kickMember(targetMemberId, mockLeader.getUsername()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ILLEGAL_ARGUMENT.getMessage());

        verify(teamMemberRepositoryCustom, never()).kickMemberById(any(Integer.class),any(Integer.class));

    }

    @Test
    @DisplayName("팀에서 탈퇴 성공")
    void leaveTeam_Success() throws Exception {
        // given
        String leaveMemberName = mockMember.getUsername();
        int targetTeamId = mockTeam.getId();

        // when
        when(memberRepository.findByUsername(leaveMemberName)).thenReturn(mockMember);
        when(teamRepository.findById(mockTeam.getId())).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepository.existsByTeamAndMember(mockTeam,mockMember)).thenReturn(true);

        MemberDto.Response response = teamService.leaveTeam(targetTeamId,mockMember.getId());

        when(teamService.leaveTeam(targetTeamId, mockMember.getId())).thenReturn(response);

        // then
        verify(teamMemberRepository).deleteMemberFromTeamById(targetTeamId,mockMember.getId());
        assertThat("member",response.getusername());
    }

    @Test
    @DisplayName("팀에서 탈퇴 실패 - 팀 멤버가 아님")
    void leaveTeam_Fail_NotTeamMember() throws Exception {
        // givn
        MemberEntity notMember = MemberEntity.builder()
                .id(123123)
                .username("notMember")
                .build();

        int targetTeamId = mockTeam.getId();
        String leaveMemberName = mockMember.getUsername();

        // when
        when(memberRepository.findByUsername(notMember.getUsername())).thenReturn(notMember);
        when(teamRepository.findById(mockTeam.getId())).thenReturn(Optional.of(mockTeam));

        when(teamMemberRepositoryCustom.findMemberInTeamById(targetTeamId,notMember.getId())).thenReturn(notMember);

        // then
        assertThatThrownBy(() -> teamService.leaveTeam(targetTeamId, leaveMemberName))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NOT_TEAM_MEMBER.getMessage());

        // then
        verify(teamMemberRepositoryCustom, never()).deleteMemberFromTeamById(targetTeamId, mockMember.getId());
    }

    @Test
    @DisplayName("팀에서 탈퇴 실패 - 리더임. 리더를 양도해야함")
    void leaveTeam_Fail_LeaderCannot() throws Exception {
        // givn
        int targetTeamId = mockTeam.getId();
        int leaveMemberId = mockLeader.getId();
        String leaveMemberName = mockLeader.getUsername();


        // when
        when(memberRepository.findByUsername(mockLeader.getUsername())).thenReturn(mockLeader);
        when(teamRepository.findById(targetTeamId)).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepositoryCustom.findMemberInTeamById(targetTeamId, leaveMemberId)).thenReturn(mockLeader);

        // then
        assertThatThrownBy(() -> teamService.leaveTeam(targetTeamId, leaveMemberName))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.LEADER_CANNOT_LEAVE.getMessage());

        // then
        verify(teamMemberRepositoryCustom, never()).deleteMemberFromTeamById(targetTeamId, leaveMemberId);
    }

    @Test
    @DisplayName("팀 리더 양도 성공")
    void changeLeader_Success() throws Exception {

    }

    @Test
    @DisplayName("팀 리더 양도 실패 - 리더가 아님")
    void changeLeader_Fail_NotLeader() throws Exception {

    }

    @Test
    @DisplayName("팀 리더 양도 실패 - 팀 멤버가 아님")
    void changeLeader_Fail_NotTeamMember() throws Exception {

    }

}