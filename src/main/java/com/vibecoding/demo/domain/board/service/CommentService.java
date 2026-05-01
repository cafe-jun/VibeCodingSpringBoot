package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.board.dto.CommentResponse;
import com.vibecoding.demo.domain.board.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.board.entity.Comment;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.CommentRepository;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createComment(Long postId, Long memberId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(request.content())
                .build();

        post.incrementCommentCount();

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        List<Comment> comments = commentRepository.findByPostIdWithMember(postId);

        return comments.stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.isDeleted() ? "삭제된 댓글입니다." : c.getContent(),
                        c.getMember().getName(),
                        c.getMember().getId(),
                        c.isDeleted(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void updateComment(Long commentId, Long memberId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(request.content());
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        if (!comment.isDeleted()) {
            comment.delete();
            comment.getPost().decrementCommentCount();
        }
    }
}
