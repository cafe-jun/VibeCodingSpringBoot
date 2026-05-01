package com.vibecoding.demo.domain.board.controller;

import com.vibecoding.demo.domain.board.dto.PostDetailResponse;
import com.vibecoding.demo.domain.board.dto.PostListResponse;
import com.vibecoding.demo.domain.board.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private com.vibecoding.demo.global.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.vibecoding.demo.global.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.vibecoding.demo.global.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("GET /api/posts 에 offset과 limit 파라미터가 정상 동작한다")
    void getPosts() throws Exception {
        // given
        PostListResponse post1 = new PostListResponse(1L, "T1", "A1", 0, LocalDateTime.now());
        given(postService.getPostsByOffsetAndLimit(2, 1)).willReturn(Arrays.asList(post1));

        // when & then
        mockMvc.perform(get("/api/posts")
                        .param("offset", "2")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("T1"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId} 가 상세 정보와 댓글을 반환한다")
    void getPostDetail() throws Exception {
        // given
        PostDetailResponse post = new PostDetailResponse(1L, "T1", "C1", "A1", 0, LocalDateTime.now(), List.of());
        given(postService.getPostDetail(1L)).willReturn(post);

        // when & then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("T1"))
                .andExpect(jsonPath("$.data.comments").isArray());
    }

    @Test
    @DisplayName("GET /api/posts/count 에 전체 게시글 개수가 정상 반환된다")
    void getPostCount() throws Exception {
        // given
        given(postService.getTotalPostCount()).willReturn(15L);

        // when & then
        mockMvc.perform(get("/api/posts/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(15));
    }
}
