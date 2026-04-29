package com.vibecoding.demo.domain.member.dto;

public record LoginResponse(
    Long id,
    String loginId,
    String name,
    String accessToken,
    String refreshToken
) {
}
