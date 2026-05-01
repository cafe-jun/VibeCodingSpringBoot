package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.dto.PostCreateRequest;
import com.vibecoding.demo.domain.board.dto.PostResponse;
import com.vibecoding.demo.domain.board.dto.PostUpdateRequest;
import com.vibecoding.demo.domain.board.service.PostService;
import com.vibecoding.demo.global.dto.ApiResponse;
import com.vibecoding.demo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostCreateRequest request) {
        Long postId = postService.createPost(request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("게시글이 등록되었습니다.", postId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostResponse> posts = postService.getPostsByOffsetAndLimit(offset, limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        PostResponse post = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostUpdateRequest request) {
        postService.updatePost(postId, request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(postId, userDetails.getMemberId(), userDetails.getMember().getRole());
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }
    
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getPostCount() {
        return ResponseEntity.ok(ApiResponse.success(postService.getTotalPostCount()));
    }
}
