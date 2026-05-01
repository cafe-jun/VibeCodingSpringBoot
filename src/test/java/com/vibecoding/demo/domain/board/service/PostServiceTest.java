package com.vibecoding.demo.domain.board.service;

import com.vibecoding.demo.domain.board.dto.PostDetailResponse;
import com.vibecoding.demo.domain.board.dto.PostListResponse;
import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.repository.PostRepository;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentService commentService;

    @Test
    @DisplayName("서비스가 리포지토리의 offset limit 메서드를 올바르게 호출한다")
    void getPostsByOffsetAndLimit() {
        // given
        Post post1 = Post.builder().title("T1").content("C1").author("A1").build();
        Post post2 = Post.builder().title("T2").content("C2").author("A2").build();
        given(postRepository.findPostsWithOffsetAndLimit(0, 2))
                .willReturn(Arrays.asList(post1, post2));

        // when
        List<PostListResponse> result = postService.getPostsByOffsetAndLimit(0, 2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("T1");
        assertThat(result.get(1).title()).isEqualTo("T2");
    }

    @Test
    @DisplayName("게시글 상세 정보를 조회한다 (댓글 포함)")
    void getPostDetail() {
        // given
        Long postId = 1L;
        Post post = Post.builder().title("T1").content("C1").author("A1").build();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentService.getComments(postId)).willReturn(List.of());

        // when
        PostDetailResponse result = postService.getPostDetail(postId);

        // then
        assertThat(result.title()).isEqualTo("T1");
        assertThat(result.comments()).isEmpty();
    }

    @Test
    @DisplayName("서비스가 리포지토리의 count 메서드를 올바르게 호출한다")
    void getTotalPostCount() {
        // given
        given(postRepository.count()).willReturn(15L);

        // when
        long count = postService.getTotalPostCount();

        // then
        assertThat(count).isEqualTo(15L);
    }
}
