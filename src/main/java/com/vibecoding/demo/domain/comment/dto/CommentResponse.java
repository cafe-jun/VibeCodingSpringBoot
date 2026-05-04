package com.vibecoding.demo.domain.comment.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String nickname,
        Long memberId,
        boolean isDeleted,
        LocalDateTime createdAt
) {
}
