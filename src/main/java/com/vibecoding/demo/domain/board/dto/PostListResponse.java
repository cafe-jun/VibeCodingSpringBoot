package com.vibecoding.demo.domain.board.dto;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String author,
        long commentCount,
        LocalDateTime createdAt
) {
}
