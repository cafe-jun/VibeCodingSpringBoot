package com.vibecoding.demo.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "member/signup";
    }
}
