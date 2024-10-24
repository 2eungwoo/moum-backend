package study.moum.moum.team.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import study.moum.auth.domain.CustomUserDetails;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.auth.dto.MemberDto;
import study.moum.global.error.exception.NeedLoginException;
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

        loginCheck(customUserDetails.getUsername());
        TeamDto.Response teamResponseDto = teamService.getTeamById(teamId);
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

        String loginUserName = loginCheck(customUserDetails.getUsername());
        TeamDto.Response teamResponseDto = teamService.createTeam(teamRequestDto, loginUserName);
        ResultResponse response = new ResultResponse(ResponseCode.CREATE_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * 팀 정보 수정 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 팀 ID
     * @param 팀 생성 요청 DTO
     */
    @PatchMapping("/api/teams/{teamId}")
    public void 팀정보수정(){

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

        String loginUserName = loginCheck(customUserDetails.getUsername());
        MemberDto.Response memberResponseDto = teamService.inviteMember(teamId, memberId, loginUserName);
        ResultResponse response = new ResultResponse(ResponseCode.INVITE_MEMBER_SUCCESS, memberResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * 팀 해체 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param 삭제할 팀 ID
     *
     */
    @DeleteMapping("/api/teams/{teamId}")
    public ResponseEntity<ResultResponse> deleteTeamById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                               @PathVariable int teamId){

        String loginUserName = loginCheck(customUserDetails.getUsername());
        TeamDto.Response teamResponseDto = teamService.deleteTeamById(teamId, loginUserName);
        ResultResponse response = new ResultResponse(ResponseCode.DELETE_TEAM_SUCCESS, teamResponseDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
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

    /**
     * 팀 리더 양도 API
     *
     * @param customUserDetails 현재 인증된 사용자 정보 (CustomUserDetails 객체에서 사용자 정보 추출)
     * @param
     *
     */
    @PatchMapping("/api/teams/change-leader/{memberId}")
    public void 팀리더넘기기(){
    }

    public String loginCheck(String username){
        if(username == null){
            throw new NeedLoginException();
        }

        return username;
    }

}
