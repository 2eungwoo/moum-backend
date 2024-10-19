package study.moum.moum.team.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import study.moum.auth.domain.CustomUserDetails;
import study.moum.auth.dto.MemberDto;
import study.moum.global.response.ResponseCode;
import study.moum.global.response.ResultResponse;
import study.moum.moum.team.dto.TeamDto;
import study.moum.moum.team.service.TeamService;

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/api/teams/{teamId}")
    public ResponseEntity<ResultResponse> getTeamById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @PathVariable int teamId){

        TeamDto.Response teamResponseDto = teamService.getTeamById(customUserDetails.getUsername(), teamId);
        ResultResponse response = new ResultResponse(ResponseCode.GET_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @PostMapping("/api/teams")
    public ResponseEntity<ResultResponse> createTeam(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @Valid @RequestBody TeamDto.Request teamRequestDto){

        TeamDto.Response teamResponseDto = teamService.createTeam(teamRequestDto, customUserDetails.getUsername());
        ResultResponse response = new ResultResponse(ResponseCode.CREATE_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    // 초대 요청 보내기가 아니고 초대 하기임. 이거 요청보내면 타겟멤버가 팀이 되는거임
    @PostMapping("/api/teams/{teamId}/invite/{memberId}")
    public ResponseEntity<ResultResponse> inviteMember(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                       @PathVariable int teamId,
                                                       @PathVariable int memberId) {

        MemberDto.Response teamResponseDto = teamService.inviteMember(teamId, memberId, customUserDetails.getUsername());
        ResultResponse response = new ResultResponse(ResponseCode.INVITE_MEMBER_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

}
