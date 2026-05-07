package com.vibecoding.demo.domain.posts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.demo.domain.posts.dto.*;
import com.vibecoding.demo.domain.posts.service.PostService;
import com.vibecoding.demo.domain.member.entity.Member;
import com.vibecoding.demo.domain.member.entity.Role;
import com.vibecoding.demo.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postApiController)
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

    // --- 게시글 테스트 ---

    @Test
    @DisplayName("로그인한 사용자는 게시글을 등록할 수 있다")
    void createPost() throws Exception {
        PostCreateRequest request = new PostCreateRequest("title", "content");
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        setAuthentication(userDetails);
        given(postService.createPost(any(PostCreateRequest.class), eq(1L))).willReturn(1L);

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
        PostListResponse post1 = new PostListResponse(1L, "T1", "tester", 0, LocalDateTime.now());
        given(postService.getPostsByOffsetAndLimit(0, 10)).willReturn(Arrays.asList(post1));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("T1"));
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void updatePost() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest("new title", "new content");
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        setAuthentication(userDetails);

        mockMvc.perform(patch("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/posts/count 에 전체 게시글 개수가 정상 반환된다")
    void getPostCount() throws Exception {
        given(postService.getTotalPostCount()).willReturn(15L);

        mockMvc.perform(get("/api/posts/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(15));
    }

    private void setAuthentication(CustomUserDetails userDetails) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
