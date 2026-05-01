package com.vibecoding.demo.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.demo.domain.board.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.board.dto.CommentResponse;
import com.vibecoding.demo.domain.board.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.board.service.CommentService;
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

import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentApiController.class)
class CommentApiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private CommentApiController commentApiController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

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
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(commentApiController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new com.vibecoding.demo.global.exception.GlobalExceptionHandler())
                .build();
    }

    private CustomUserDetails createCustomUserDetails() {
        Member member = Member.builder()
                .loginId("testuser")
                .password("password")
                .name("tester")
                .email("test@test.com")
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);
        return new CustomUserDetails(member);
    }

    @Test
    @DisplayName("댓글을 생성한다")
    void createComment() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("content");
        CustomUserDetails userDetails = createCustomUserDetails();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        given(commentService.createComment(eq(1L), eq(1L), any(CommentCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("댓글 목록을 조회한다")
    void getComments() throws Exception {
        // given
        CommentResponse response = new CommentResponse(1L, "content", "tester", 1L, false, LocalDateTime.now());
        given(commentService.getComments(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("content"));
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void updateComment() throws Exception {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("new content");
        CustomUserDetails userDetails = createCustomUserDetails();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // when & then
        mockMvc.perform(patch("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 수정되었습니다."));
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment() throws Exception {
        // given
        CustomUserDetails userDetails = createCustomUserDetails();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // when & then
        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));
    }
}
