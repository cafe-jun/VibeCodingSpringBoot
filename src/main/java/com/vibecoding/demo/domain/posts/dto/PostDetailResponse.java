package com.vibecoding.demo.domain.posts.dto;

import com.vibecoding.demo.domain.comment.dto.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String authorName,
        long commentCount,
        long viewCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {
}
