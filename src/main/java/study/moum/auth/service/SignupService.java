package study.moum.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.auth.domain.repository.MemberRepository;
import study.moum.auth.dto.MemberDto;
import study.moum.global.error.ErrorCode;
import study.moum.global.error.exception.CustomException;
import study.moum.global.error.exception.DuplicateUsernameException;
import study.moum.config.redis.util.RedisUtil;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisUtil redisUtil;

    public void signupMember(MemberDto.Request memberRequestDto){

        Boolean isExist = memberRepository.existsByUsername(memberRequestDto.getUsername());
        if(isExist){
            throw new DuplicateUsernameException();
        }
        // dto -> entity
        MemberEntity memberEntity = MemberEntity.builder()
                .id(memberRequestDto.getId())
                .username(memberRequestDto.getUsername())
                .email(memberRequestDto.getEmail())
                .role("ROLE_ADMIN")
                .password(bCryptPasswordEncoder.encode(memberRequestDto.getPassword()))
                .address(memberRequestDto.getAddress())
                .build();

        // 인증 코드 검증
        String verifyCode = redisUtil.getData(memberRequestDto.getEmail()); // Redis에서 이메일로 인증 코드 가져오기
        if (verifyCode == null || !verifyCode.equals(memberRequestDto.getVerifyCode())) {
            System.out.println("==============="+verifyCode);
            throw new CustomException(ErrorCode.EMAIL_VERIFY_FAILED);
        }


        memberRepository.save(memberEntity);

    }

}
