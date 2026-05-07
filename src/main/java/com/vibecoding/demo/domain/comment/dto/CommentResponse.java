package com.vibecoding.demo.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        String nickname,
        Long memberId,
        boolean isDeleted,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
}
