package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.PostDetailResponse;
import com.vibecoding.demo.domain.board.dto.PostListResponse;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentService commentService;

    public List<PostListResponse> getPostsByOffsetAndLimit(int offset, int limit) {
        return postRepository.findPostsWithOffsetAndLimit(offset, limit).stream()
                .map(post -> new PostListResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getAuthor(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getCommentCount(),
                post.getCreatedAt(),
                commentService.getComments(postId)
        );
    }
    
    public long getTotalPostCount() {
        return postRepository.count();
    }
    
    @Transactional
    public Post createPost(String title, String content, String author) {
        return postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build());
    }
}
