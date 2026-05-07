package com.vibecoding.demo.domain.posts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/write")
    public String writeForm() {
        return "posts/create";
    }

    @GetMapping("/{postId}")
    public String postDetail(@PathVariable Long postId, Model model) {
        model.addAttribute("postId", postId);
        return "posts/detail";
    }

    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long postId, Model model) {
        model.addAttribute("postId", postId);
        return "posts/edit";
    }
}
