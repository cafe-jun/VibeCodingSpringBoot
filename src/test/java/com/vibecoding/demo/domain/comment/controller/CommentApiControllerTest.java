package com.vibecoding.demo.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.demo.domain.comment.dto.CommentCreateRequest;
import com.vibecoding.demo.domain.comment.dto.CommentResponse;
import com.vibecoding.demo.domain.comment.dto.CommentUpdateRequest;
import com.vibecoding.demo.domain.comment.service.CommentService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentApiController)
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
    @DisplayName("댓글을 등록한다")
    void createComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest("comment content", null);
        CustomUserDetails userDetails = createUserDetails(1L, Role.USER);
        setAuthentication(userDetails);
        given(commentService.createComment(eq(1L), eq(1L), any(CommentCreateRequest.class))).willReturn(1L);

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
        CommentResponse comment = new CommentResponse(1L, "content", "tester", 1L, false, LocalDateTime.now(), List.of());
        given(commentService.getComments(1L)).willReturn(List.of(comment));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("content"));
    }

    private void setAuthentication(CustomUserDetails userDetails) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
