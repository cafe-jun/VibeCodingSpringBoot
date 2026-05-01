package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.PostCreateRequest;
import com.vibecoding.demo.domain.board.dto.PostResponse;
import com.vibecoding.demo.domain.board.dto.PostUpdateRequest;
import com.vibecoding.demo.domain.board.entity.Post;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member createMember(Long id, String name, Role role) {
        Member member = Member.builder()
                .loginId("user" + id)
                .password("pass")
                .name(name)
                .email("email" + id + "@test.com")
                .role(role)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Test
    @DisplayName("게시글을 성공적으로 등록한다")
    void createPost() {
        // given
        Long memberId = 1L;
        PostCreateRequest request = new PostCreateRequest("title", "content");
        Member member = createMember(memberId, "tester", Role.USER);
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        postService.createPost(request, memberId);

        // then
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void getPostsByOffsetAndLimit() {
        // given
        Member member = createMember(1L, "tester", Role.USER);
        Post post1 = Post.builder().title("T1").content("C1").member(member).build();
        Post post2 = Post.builder().title("T2").content("C2").member(member).build();
        given(postRepository.findPostsWithOffsetAndLimit(0, 2))
                .willReturn(Arrays.asList(post1, post2));

        // when
        List<PostResponse> result = postService.getPostsByOffsetAndLimit(0, 2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("T1");
        assertThat(result.get(0).authorName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("작성자 본인은 게시글을 수정할 수 있다")
    void updatePost_success() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        PostUpdateRequest request = new PostUpdateRequest("new title", "new content");
        Member member = createMember(memberId, "tester", Role.USER);
        Post post = Post.builder().title("old").content("old").member(member).build();
        
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postService.updatePost(postId, request, memberId);

        // then
        assertThat(post.getTitle()).isEqualTo("new title");
        assertThat(post.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("작성자가 아니면 게시글을 수정할 수 없다")
    void updatePost_fail() {
        // given
        Long postId = 1L;
        Long authorId = 1L;
        Long otherId = 2L;
        PostUpdateRequest request = new PostUpdateRequest("new title", "new content");
        Member author = createMember(authorId, "author", Role.USER);
        Post post = Post.builder().title("old").content("old").member(author).build();
        
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, request, otherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 게시글을 삭제할 수 있다")
    void deletePost_admin() {
        // given
        Long postId = 1L;
        Long authorId = 1L;
        Long adminId = 99L;
        Member author = createMember(authorId, "author", Role.USER);
        Post post = Post.builder().title("T").content("C").member(author).build();
        
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postService.deletePost(postId, adminId, Role.ADMIN);

        // then
        verify(postRepository).delete(post);
    }
}
