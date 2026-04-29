package com.vibecoding.demo.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {
}
