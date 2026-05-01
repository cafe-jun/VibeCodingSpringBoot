package com.vibecoding.demo.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        String nickname,
        Long memberId,
        boolean isDeleted,
        LocalDateTime createdAt
) {
}
