package com.vibecoding.demo.domain.member.controller;

import com.vibecoding.demo.domain.member.dto.LoginRequest;
import com.vibecoding.demo.domain.member.dto.LoginResponse;
import com.vibecoding.demo.domain.member.dto.SignupRequest;
import com.vibecoding.demo.domain.member.dto.SignupResponse;
import com.vibecoding.demo.domain.member.service.MemberService;
import com.vibecoding.demo.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", memberService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", memberService.login(request)));
    }
}
