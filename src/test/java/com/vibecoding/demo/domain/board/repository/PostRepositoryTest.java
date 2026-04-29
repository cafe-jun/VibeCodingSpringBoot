package com.vibecoding.demo.domain.board.repository;

import com.vibecoding.demo.domain.board.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("offset과 limit으로 게시글을 조회할 수 있다")
    void findPostsWithOffsetAndLimit() {
        // given
        for (int i = 1; i <= 15; i++) {
            postRepository.save(Post.builder()
                    .title("Title " + i)
                    .content("Content " + i)
                    .author("Author")
                    .build());
        }

        // when (offset=5, limit=5 => 최신순이므로 15~1 중 ID 내림차순(최신)으로.
        // 현재 native query: SELECT * FROM posts ORDER BY id DESC OFFSET 5 LIMIT 5
        // 전체 ID 역순: 15, 14, 13, 12, 11, [10, 9, 8, 7, 6], 5, 4, 3, 2, 1
        List<Post> result = postRepository.findPostsWithOffsetAndLimit(5, 5);

        // then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getTitle()).isEqualTo("Title 10");
        assertThat(result.get(4).getTitle()).isEqualTo("Title 6");
    }
}
