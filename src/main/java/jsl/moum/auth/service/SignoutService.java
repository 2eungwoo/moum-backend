package jsl.moum.auth.service;

import jsl.moum.auth.domain.entity.MemberEntity;
import jsl.moum.auth.domain.repository.MemberRepository;
import jsl.moum.auth.dto.MemberDto;
import jsl.moum.global.error.ErrorCode;
import jsl.moum.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignoutService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto.Response signoutMember(String username) {
        MemberEntity member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_EXIST);
        }

        member.changeActiveStatusFalse();
        return new MemberDto.Response(member);
    }
}
