package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public List<Post> getPostsByOffsetAndLimit(int offset, int limit) {
        return postRepository.findPostsWithOffsetAndLimit(offset, limit);
    }
    
    public long getTotalPostCount() {
        return postRepository.count();
    }
    
    public List<Post> getPostsByCursor(Long lastId, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        if (lastId == null) {
            return postRepository.findLatestPosts(pageable);
        }
        return postRepository.findByIdLessThanOrderByIdDesc(lastId, pageable);
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
