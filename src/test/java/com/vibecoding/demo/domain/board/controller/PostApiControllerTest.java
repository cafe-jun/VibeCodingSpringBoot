package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.entity.Post;
import com.vibecoding.demo.domain.board.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostApiController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 비활성화 (보안 의존성 충돌 방지)
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("GET /api/posts 에 offset과 limit 파라미터가 정상 동작한다")
    void getPosts() throws Exception {
        // given
        Post post1 = Post.builder().title("T1").content("C1").author("A1").build();
        given(postService.getPostsByOffsetAndLimit(2, 1)).willReturn(Arrays.asList(post1));

        // when & then
        mockMvc.perform(get("/api/posts")
                        .param("offset", "2")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("T1"));
    }
}
