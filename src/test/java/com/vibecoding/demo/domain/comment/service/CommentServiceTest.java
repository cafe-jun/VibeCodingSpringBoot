package com.vibecoding.demo.domain.comment.service;

import com.vibecoding.demo.domain.posts.entity.Post;
import com.vibecoding.demo.domain.posts.repository.PostRepository;
import com.vibecoding.demo.domain.comment.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.comment.dto.CommentResponse;
import com.vibecoding.demo.domain.comment.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.comment.entity.Comment;
import com.vibecoding.demo.domain.comment.repository.CommentRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        CommentCreateRequest request = new CommentCreateRequest("content", null);
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        Post post = Post.builder().title("title").content("content").member(member).build();
        
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
    @DisplayName("계층형 댓글(Infinite-Depth) 목록을 조회한다")
    void getComments() {
        // given
        Long postId = 1L;
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        Post post = Post.builder().title("title").content("content").member(member).build();
        
        Comment depth1 = Comment.builder().post(post).member(member).content("depth1").build();
        org.springframework.test.util.ReflectionTestUtils.setField(depth1, "id", 1L);
        
        Comment depth2 = Comment.builder().post(post).member(member).content("depth2").parent(depth1).build();
        org.springframework.test.util.ReflectionTestUtils.setField(depth2, "id", 2L);

        Comment depth3 = Comment.builder().post(post).member(member).content("depth3").parent(depth2).build();
        org.springframework.test.util.ReflectionTestUtils.setField(depth3, "id", 3L);

        given(postRepository.existsById(postId)).willReturn(true);
        given(commentRepository.findByPostIdWithMemberAndParent(postId)).willReturn(List.of(depth1, depth2, depth3));

        // when
        List<CommentResponse> result = commentService.getComments(postId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("depth1");
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).content()).isEqualTo("depth2");
        assertThat(result.get(0).children().get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).children().get(0).content()).isEqualTo("depth3");
    }

    @Test
    @DisplayName("3단계 이상의 깊은 댓글도 성공적으로 생성한다")
    void createComment_infinite_depth() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        CommentCreateRequest request = new CommentCreateRequest("depth 3", 2L);
        
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        Post post = Post.builder().title("title").content("content").member(member).build();
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", postId);
        
        Comment depth1 = Comment.builder().post(post).member(member).content("depth1").build();
        org.springframework.test.util.ReflectionTestUtils.setField(depth1, "id", 1L);

        Comment depth2 = Comment.builder().post(post).member(member).content("depth2").parent(depth1).build();
        org.springframework.test.util.ReflectionTestUtils.setField(depth2, "id", 2L);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.findById(2L)).willReturn(Optional.of(depth2));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.createComment(postId, memberId, request);

        // then
        verify(commentRepository).save(any(Comment.class));
        assertThat(post.getCommentCount()).isEqualTo(1L);
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

        Post post = Post.builder().title("T").content("C").member(member).build();
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
