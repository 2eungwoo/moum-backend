package study.moum.moum.team.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.auth.dto.MemberDto;
import study.moum.community.article.domain.article_details.ArticleDetailsEntity;
import study.moum.community.comment.controller.CommentController;
import study.moum.community.comment.domain.CommentEntity;
import study.moum.community.comment.dto.CommentDto;
import study.moum.community.comment.service.CommentService;
import study.moum.custom.WithAuthUser;
import study.moum.global.response.ResponseCode;
import study.moum.moum.team.domain.TeamEntity;
import study.moum.moum.team.domain.TeamMemberEntity;
import study.moum.moum.team.dto.TeamDto;
import study.moum.moum.team.service.TeamService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @MockBean
    private TeamService teamService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MemberEntity member;
    private MemberEntity leader;
    private TeamEntity team;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        leader = MemberEntity.builder()
                .id(1)
                .email("test@user.com")
                .username("testuser")
                .teams(new ArrayList<>())
                .build();

        member = MemberEntity.builder()
                .id(2)
                .email("member@user.com")
                .username("memberuser")
                .teams(new ArrayList<>())
                .build();

        team = TeamEntity.builder()
                .id(1)
                .leaderId(leader.getId())
                .members(new ArrayList<>())
                .teamname("test team")
                .description("test description")
                .build();


    }


    @Test
    @DisplayName("팀 생성 테스트")
    @WithAuthUser
    @Disabled("팀원 넣고 팀원리스트 뽑는 테스트 추가해야함")
    void create_team_success() throws Exception {
        // given
        TeamDto.Request teamRequest = TeamDto.Request.builder()
                .teamname("test team")
                .description("test description")
                .leaderId(leader.getId())
                .build();

        // Mock 응답 생성
        TeamDto.Response response = new TeamDto.Response(team);

        // when
        when(teamService.createTeam(any(TeamDto.Request.class), any(String.class))).thenReturn(response);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest))
                        .with(csrf()))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(ResponseCode.CREATE_TEAM_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.teamName").value("test team"))
                .andExpect(jsonPath("$.data.leaderId").value(leader.getId()));
    }


    @Test
    @DisplayName("팀 정보 조회(단건) 테스트")
    @WithAuthUser
    void get_team_info_success() throws Exception{
        // given
        TeamDto.Response response = new TeamDto.Response(team);

        // when
        when(teamService.getTeamById(team.getId())).thenReturn(response);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/teams/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(ResponseCode.GET_TEAM_SUCCESS.getMessage()));
//                .andExpect(jsonPath("$.teamName").value(response.getTeamName()))
//                .andExpect(jsonPath("$.description").value(response.getDescription()));
    }
//
//    @Test
//    @DisplayName("모든 팀 리스트 조회 테스트")
//    @WithAuthUser
//    void get_team_list_success() throws Exception {
//        // given
//        TeamEntity anotherTeam = TeamEntity.builder()
//                .id(2)
//                .leaderId(leader.getId())
//                .teamname("another team")
//                .description("another description")
//                .build();
//
//        List<TeamEntity> teams = new ArrayList<>();
//        teams.add(team);
//        teams.add(anotherTeam);
//
//        List<TeamDto.Response> responseList = new ArrayList<>();
//        for (TeamEntity t : teams) {
//            responseList.add(new TeamDto.Response(t));
//        }
//
//        // when
//        when(teamService.getTeamList()).thenReturn(responseList);
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/teams")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value(ResponseCode.GET_TEAM_LIST_SUCCESS.getMessage()))
//                .andExpect(jsonPath("$.teams").isArray())
//                .andExpect(jsonPath("$.teams.length()").value(responseList.size()))
//                .andExpect(jsonPath("$.teams[0].teamName").value(team.getTeamname()))
//                .andExpect(jsonPath("$.teams[0].leaderId").value(team.getLeaderId()))
//                .andExpect(jsonPath("$.teams[1].teamName").value("another team"))
//                .andExpect(jsonPath("$.teams[1].leaderId").value(anotherTeam.getLeaderId()));
//    }
//
//
//    @Test
//    @DisplayName("팀 정보 수정 테스트")
//    @WithAuthUser
//    @Disabled
//    void update_team_info_success() throws Exception{
//        // given
//        TeamDto.Request updateRequest = TeamDto.Request.builder()
//                .teamname("update team")
//                .description("update description")
//                .leaderId(leader.getId())
//                .build();
//
//        TeamDto.Response response = new TeamDto.Response(team);
//
//        // when
//        when(teamService.updateTeam(updateRequest, leader.getId())).thenReturn(response);
//
//        // then
//        mockMvc.perform(MockMvcRequestBuilders.patch("/api/team/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest))
//                        .with(csrf()))
//                .andExpect(jsonPath("$.status").value(201))
//                .andExpect(jsonPath("$.message").value(ResponseCode.UPDATE_TEAM_SUCCESS.getMessage()))
//                .andExpect(jsonPath("$.teamName").value(updateRequest.getTeamname()))
//                .andExpect(jsonPath("$.description").value(updateRequest.getDescription()));
//    }

    @Test
    @DisplayName("나의 팀 리스트 조회 테스트")
    @WithAuthUser
    void get_my_team_list_success() throws Exception{

    }


    @Test
    @DisplayName("팀 삭제 테스트")
    @WithAuthUser
    void delete_team_success() throws Exception{

    }

    @Test
    @DisplayName("팀에 멤버 초대 테스트")
    @WithAuthUser
    void invite_member_success() throws Exception{
        // given
        int teamId = team.getId();

        MemberEntity targetMember = MemberEntity.builder()
                .id(222)
                .username("new member")
                .build();

        MemberDto.Response memberResponse = new MemberDto.Response(targetMember);

        // when
        when(teamService.inviteMember(anyInt(), anyInt(), anyString())).thenReturn(memberResponse);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teams/{teamId}/invite/{memberId}", teamId, targetMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(ResponseCode.INVITE_MEMBER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(targetMember.getId()))
                .andExpect(jsonPath("$.data.username").value("new member"));
    }

    @Test
    @DisplayName("팀에서 멤버 추방 테스트")
    @WithAuthUser
    void exile_member_success() throws Exception{

    }




}