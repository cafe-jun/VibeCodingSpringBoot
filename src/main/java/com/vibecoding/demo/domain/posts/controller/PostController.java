package com.vibecoding.demo.domain.posts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/posts")
public class PostController {

    @GetMapping
    public String boardList() {
        return "posts/list";
    }

    @GetMapping("/infinite")
    public String infiniteBoardList() {
        return "posts/infinite-list";
    }
}
