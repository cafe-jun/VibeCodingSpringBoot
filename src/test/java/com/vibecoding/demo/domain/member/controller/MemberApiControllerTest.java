package com.vibecoding.demo.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.demo.domain.member.dto.LoginRequest;
import com.vibecoding.demo.domain.member.dto.SignupResponse;
import com.vibecoding.demo.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("로그인 실패 시 400 에러와 함께 에러 메시지를 반환한다")
    void login_fail_response() throws Exception {
        // given
        LoginRequest request = new LoginRequest("failuser", "wrongpass");
        given(memberService.login(any(LoginRequest.class)))
                .willThrow(new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
