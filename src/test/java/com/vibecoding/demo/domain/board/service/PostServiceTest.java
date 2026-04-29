package com.vibecoding.demo.domain.board.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("서비스가 리포지토리의 offset limit 메서드를 올바르게 호출한다")
    void getPostsByOffsetAndLimit() {
        // given
        Post post1 = Post.builder().title("T1").content("C1").author("A1").build();
        Post post2 = Post.builder().title("T2").content("C2").author("A2").build();
        given(postRepository.findPostsWithOffsetAndLimit(0, 2))
                .willReturn(Arrays.asList(post1, post2));

        // when
        List<Post> result = postService.getPostsByOffsetAndLimit(0, 2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("T1");
        assertThat(result.get(1).getTitle()).isEqualTo("T2");
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
