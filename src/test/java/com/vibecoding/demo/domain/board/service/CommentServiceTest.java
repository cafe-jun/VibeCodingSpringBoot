package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.board.dto.CommentResponse;
import com.vibecoding.demo.domain.board.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.board.entity.Comment;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.CommentRepository;
import com.vibecoding.demo.domain.board.repository.PostRepository;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.entity.Role;
import com.vibecoding.demo.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("댓글을 성공적으로 생성한다")
    void createComment() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        CommentCreateRequest request = new CommentCreateRequest("content");
        Post post = Post.builder().title("title").content("content").author("author").build();
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.createComment(postId, memberId, request);

        // then
        verify(commentRepository).save(any(Comment.class));
        assertThat(post.getCommentCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글 목록을 조회한다")
    void getComments() {
        // given
        Long postId = 1L;
        Post post = Post.builder().title("title").content("content").author("author").build();
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        
        Comment comment1 = Comment.builder().post(post).member(member).content("comment1").build();
        Comment comment2 = Comment.builder().post(post).member(member).content("comment2").build();

        given(postRepository.existsById(postId)).willReturn(true);
        given(commentRepository.findByPostIdWithMember(postId)).willReturn(List.of(comment1, comment2));

        // when
        List<CommentResponse> result = commentService.getComments(postId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("comment1");
        assertThat(result.get(1).content()).isEqualTo("comment2");
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void updateComment() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;
        CommentUpdateRequest request = new CommentUpdateRequest("new content");
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", memberId);

        Comment comment = Comment.builder()
                .member(member)
                .content("old content")
                .build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.updateComment(commentId, memberId, request);

        // then
        assertThat(comment.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("댓글을 삭제(소프트 딜리트)한다")
    void deleteComment() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", memberId);

        Post post = Post.builder().title("T").content("C").author("A").build();
        post.incrementCommentCount();

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content("content")
                .build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId, memberId);

        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(post.getCommentCount()).isEqualTo(0L);
    }
}
