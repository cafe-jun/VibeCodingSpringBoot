package com.vibecoding.demo.domain.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String author,
        long commentCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {
}
