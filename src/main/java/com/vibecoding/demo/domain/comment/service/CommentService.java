package com.vibecoding.demo.domain.comment.service;

import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import com.vibecoding.demo.domain.comment.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.comment.dto.CommentResponse;
import com.vibecoding.demo.domain.comment.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.comment.entity.Comment;
import com.vibecoding.demo.domain.comment.repository.CommentRepository;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
            
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("댓글은 최대 2단계(대댓글)까지만 작성 가능합니다.");
            }
            
            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("부모 댓글과 게시글 정보가 일치하지 않습니다.");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(request.content())
                .parent(parent)
                .build();

        post.incrementCommentCount();

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        List<Comment> comments = commentRepository.findByPostIdWithMemberAndParent(postId);
        Map<Long, CommentResponse> map = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        comments.forEach(c -> {
            CommentResponse response = new CommentResponse(
                    c.getId(),
                    c.isDeleted() ? "삭제된 댓글입니다." : c.getContent(),
                    c.getMember().getName(),
                    c.getMember().getId(),
                    c.isDeleted(),
                    c.getCreatedAt(),
                    new ArrayList<>()
            );
            
            map.put(response.id(), response);
            
            if (c.getParent() == null) {
                roots.add(response);
            } else {
                CommentResponse parentResponse = map.get(c.getParent().getId());
                if (parentResponse != null) {
                    parentResponse.children().add(response);
                }
            }
        });

        return roots;
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
