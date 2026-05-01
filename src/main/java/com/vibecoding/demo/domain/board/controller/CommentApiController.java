package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.board.dto.CommentResponse;
import com.vibecoding.demo.domain.board.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.board.service.CommentService;
import com.vibecoding.demo.global.dto.ApiResponse;
import com.vibecoding.demo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {
        Long commentId = commentService.createComment(postId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("댓글이 등록되었습니다.", commentId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentUpdateRequest request) {
        commentService.updateComment(commentId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", null));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }
}
