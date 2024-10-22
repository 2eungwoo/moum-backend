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

    /**
     * 팀 조회 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 팀 ID
     *
     */
    @GetMapping("/api/teams/{teamId}")
    public ResponseEntity<ResultResponse> getTeamById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @PathVariable int teamId){

        TeamDto.Response teamResponseDto = teamService.getTeamById(customUserDetails.getUsername(), teamId);
        ResultResponse response = new ResultResponse(ResponseCode.GET_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * 팀 생성 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 팀 생성 요청 DTO
     *
     */
    @PostMapping("/api/teams")
    public ResponseEntity<ResultResponse> createTeam(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @Valid @RequestBody TeamDto.Request teamRequestDto){

        TeamDto.Response teamResponseDto = teamService.createTeam(teamRequestDto, customUserDetails.getUsername());
        ResultResponse response = new ResultResponse(ResponseCode.CREATE_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }


    // todo : 멤버 초대하기 -> 알람발송, 팀 가입요청 수락 -> 가입성공 으로 로직 변경
    /**
     * 팀에 멤버 초대 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 팀 ID
     * @param 멤버 ID
     */
    // 초대 요청 보내기가 아니고 초대 하기임. 이거 요청보내면 타겟멤버가 팀이 되는거임
    @PostMapping("/api/teams/{teamId}/invite/{memberId}")
    public ResponseEntity<ResultResponse> inviteMember(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                       @PathVariable int teamId,
                                                       @PathVariable int memberId) {

        MemberDto.Response teamResponseDto = teamService.inviteMember(teamId, memberId, customUserDetails.getUsername());
        ResultResponse response = new ResultResponse(ResponseCode.INVITE_MEMBER_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
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

}
