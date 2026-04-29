package com.vibecoding.demo.domain.member.service;

import com.vibecoding.demo.domain.member.dto.LoginRequest;
import com.vibecoding.demo.domain.member.dto.LoginResponse;
import com.vibecoding.demo.domain.member.dto.SignupRequest;
import com.vibecoding.demo.domain.member.dto.SignupResponse;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import com.vibecoding.demo.global.security.CustomUserDetails;
import com.vibecoding.demo.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

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

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password());
        
        // AuthenticationManager를 통한 검증 (내부적으로 CustomUserDetailsService 호출)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(
                userDetails.getMemberId(),
                userDetails.getLoginId(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );

        return new LoginResponse(
                userDetails.getMemberId(),
                userDetails.getLoginId(),
                userDetails.getName(),
                accessToken
        );
    }
}
