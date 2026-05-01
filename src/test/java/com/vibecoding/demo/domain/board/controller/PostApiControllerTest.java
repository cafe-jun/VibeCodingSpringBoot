package com.vibecoding.demo.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.demo.domain.board.dto.PostCreateRequest;
import com.vibecoding.demo.domain.board.dto.PostResponse;
import com.vibecoding.demo.domain.board.dto.PostUpdateRequest;
import com.vibecoding.demo.domain.board.service.PostService;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.entity.Role;
import com.vibecoding.demo.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostApiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private PostApiController postApiController;

    @Autowired
    private ObjectMapper objectMapper;

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

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(postApiController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new com.vibecoding.demo.global.exception.GlobalExceptionHandler())
                .build();
    }

    private CustomUserDetails createUserDetails(Long id, Role role) {
        Member member = Member.builder()
                .loginId("user" + id)
                .password("pass")
                .name("tester")
                .email("test@test.com")
                .role(role)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", id);
        return new CustomUserDetails(member);
    }

    @Test
    @DisplayName("로그인한 사용자는 게시글을 등록할 수 있다")
    void createPost() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest("title", "content");
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        given(postService.createPost(any(PostCreateRequest.class), eq(1L))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void getPosts() throws Exception {
        // given
        PostResponse post1 = new PostResponse(1L, "T1", "C1", "A1", LocalDateTime.now());
        given(postService.getPostsByOffsetAndLimit(0, 10)).willReturn(Arrays.asList(post1));

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("T1"));
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void updatePost() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest("new title", "new content");
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // when & then
        mockMvc.perform(patch("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 수정되었습니다."));
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void deletePost() throws Exception {
        // given
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // when & then
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));
    }
}
