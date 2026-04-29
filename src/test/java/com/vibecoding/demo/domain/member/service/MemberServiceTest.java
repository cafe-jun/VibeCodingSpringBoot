package com.vibecoding.demo.domain.member.service;

import com.vibecoding.demo.domain.member.dto.SignupRequest;
import com.vibecoding.demo.domain.member.dto.SignupResponse;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 - 비밀번호 암호화 및 응답 데이터 확인")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest(
                "testuser",
                "password123!",
                "테스트유저",
                "test@example.com"
        );

        // when
        SignupResponse response = memberService.signup(request);

        // then
        var savedMember = memberRepository.findById(response.id()).orElseThrow();
        assertThat(savedMember.getLoginId()).isEqualTo("testuser");
        assertThat(savedMember.getName()).isEqualTo("테스트유저");
        assertThat(savedMember.getEmail()).isEqualTo("test@example.com");
        assertThat(savedMember.getRole()).isEqualTo(com.vibecoding.demo.domain.member.entity.Role.USER);
        
        // 응답 데이터 확인
        assertThat(response.loginId()).isEqualTo("testuser");
        assertThat(response.name()).isEqualTo("테스트유저");
        
        // 비밀번호 암호화 확인
        assertThat(passwordEncoder.matches("password123!", savedMember.getPassword())).isTrue();
        assertThat(savedMember.getPassword()).isNotEqualTo("password123!");
    }
}
