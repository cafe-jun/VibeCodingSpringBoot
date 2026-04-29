package com.vibecoding.demo.domain.member.service;

import com.vibecoding.demo.domain.member.dto.LoginRequest;
import com.vibecoding.demo.domain.member.dto.SignupRequest;
import com.vibecoding.demo.domain.member.dto.SignupResponse;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        var member = Member.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .build();

        var savedMember = memberRepository.save(member);
        return new SignupResponse(
                savedMember.getId(),
                savedMember.getLoginId(),
                savedMember.getName()
        );
    }

    public SignupResponse login(LoginRequest request) {
        var member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return new SignupResponse(
                member.getId(),
                member.getLoginId(),
                member.getName()
        );
    }
}
