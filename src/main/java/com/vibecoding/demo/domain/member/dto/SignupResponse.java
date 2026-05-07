package com.vibecoding.demo.domain.member.dto;

public record SignupResponse(
    Long id,
    String loginId,
    String name
) {
}
