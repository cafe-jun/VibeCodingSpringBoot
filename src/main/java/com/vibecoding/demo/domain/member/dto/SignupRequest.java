package com.vibecoding.demo.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank String loginId,
    @NotBlank String password,
    @NotBlank String name,
    @NotBlank @Email String email
) {
}
