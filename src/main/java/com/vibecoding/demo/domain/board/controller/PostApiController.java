package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.dto.PostDetailResponse;
import com.vibecoding.demo.domain.board.dto.PostListResponse;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.service.PostService;
import com.vibecoding.demo.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostListResponse> posts = postService.getPostsByOffsetAndLimit(offset, limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        PostDetailResponse post = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getPostCount() {
        return ResponseEntity.ok(ApiResponse.success(postService.getTotalPostCount()));
    }
    
    // For testing/dummy data creation
    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestParam String title, 
            @RequestParam String content, 
            @RequestParam String author) {
        return ResponseEntity.ok(postService.createPost(title, content, author));
    }
}
