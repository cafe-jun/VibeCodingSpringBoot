package com.vibecoding.demo.domain.comment.service;

import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
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
    @DisplayName("계층형 댓글(2-Depth) 목록을 조회한다")
    void getComments() {
        // given
        Long postId = 1L;
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        Post post = Post.builder().title("title").content("content").member(member).build();
        
        Comment parent = Comment.builder().post(post).member(member).content("parent").build();
        org.springframework.test.util.ReflectionTestUtils.setField(parent, "id", 1L);
        
        Comment child = Comment.builder().post(post).member(member).content("child").parent(parent).build();
        org.springframework.test.util.ReflectionTestUtils.setField(child, "id", 2L);

        given(postRepository.existsById(postId)).willReturn(true);
        given(commentRepository.findByPostIdWithMemberAndParent(postId)).willReturn(List.of(parent, child));

        // when
        List<CommentResponse> result = commentService.getComments(postId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("parent");
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).content()).isEqualTo("child");
    }

    @Test
    @DisplayName("3단계 이상의 댓글 작성 시도 시 예외가 발생한다")
    void createComment_depth_limit() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        CommentCreateRequest request = new CommentCreateRequest("depth 3", 2L);
        
        Member member = Member.builder().loginId("user").password("pass").name("name").email("email").role(Role.USER).build();
        Post post = Post.builder().title("title").content("content").member(member).build();
        
        Comment parent = Comment.builder().post(post).member(member).content("parent").build();
        Comment child = Comment.builder().post(post).member(member).content("child").parent(parent).build();
        org.springframework.test.util.ReflectionTestUtils.setField(child, "id", 2L);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.findById(2L)).willReturn(Optional.of(child));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, memberId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글은 최대 2단계(대댓글)까지만 작성 가능합니다.");
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
