package com.vibecoding.demo.domain.posts.dto;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String authorName,
        long commentCount,
        long viewCount,
        LocalDateTime createdAt
) {
}
