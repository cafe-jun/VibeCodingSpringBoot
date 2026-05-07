package com.vibecoding.demo.domain.board.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createdAt
) {
}
