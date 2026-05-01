package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.PostCreateRequest;
import com.vibecoding.demo.domain.board.dto.PostResponse;
import com.vibecoding.demo.domain.board.dto.PostUpdateRequest;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.entity.Role;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Transactional
    public Long createPost(PostCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .member(member)
                .build();

        return postRepository.save(post).getId();
    }

    public List<PostResponse> getPostsByOffsetAndLimit(int offset, int limit) {
        return postRepository.findPostsWithOffsetAndLimit(offset, limit).stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getMember().getName(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getName(),
                post.getCreatedAt()
        );
    }

    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(request.title(), request.content());
    }

    @Transactional
    public void deletePost(Long postId, Long memberId, Role role) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getMember().getId().equals(memberId) && role != Role.ADMIN) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
    
    public long getTotalPostCount() {
        return postRepository.count();
    }
}
