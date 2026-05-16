package com.vibecoding.demo.domain.posts.controller;

import com.vibecoding.demo.domain.posts.dto.*;
import com.vibecoding.demo.domain.posts.service.PostService;
import com.vibecoding.demo.global.dto.ApiResponse;
import com.vibecoding.demo.global.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
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
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostListResponse> posts = postService.getPostsByOffsetAndLimit(offset, limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        handleViewCount(postId, request, response);
        PostDetailResponse post = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    private void handleViewCount(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie oldCookie = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("viewed_posts")) {
                    oldCookie = cookie;
                    break;
                }
            }
        }

        if (oldCookie == null) {
            postService.increaseViewCount(postId);
            Cookie newCookie = new Cookie("viewed_posts", "[" + postId + "]");
            newCookie.setPath("/");
            newCookie.setHttpOnly(true);
            newCookie.setMaxAge((int) getSecondsUntilMidnight());
            response.addCookie(newCookie);
        } else {
            if (!oldCookie.getValue().contains("[" + postId + "]")) {
                postService.increaseViewCount(postId);
                oldCookie.setValue(oldCookie.getValue() + "[" + postId + "]");
                oldCookie.setPath("/");
                oldCookie.setHttpOnly(true);
                oldCookie.setMaxAge((int) getSecondsUntilMidnight());
                response.addCookie(oldCookie);
            }
        }
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
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
    
    @GetMapping("/cursor")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPostsByCursor(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit) {
        List<PostListResponse> posts = postService.getPostsByCursor(lastId, limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getPostCount() {
        return ResponseEntity.ok(ApiResponse.success(postService.getTotalPostCount()));
    }
}
