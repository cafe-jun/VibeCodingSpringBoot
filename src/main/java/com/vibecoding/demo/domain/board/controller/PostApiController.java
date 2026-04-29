package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.service.PostService;
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
    public ResponseEntity<List<Post>> getPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        List<Post> posts = postService.getPostsByOffsetAndLimit(offset, limit);
        return ResponseEntity.ok(posts);
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
