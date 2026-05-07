package com.vibecoding.demo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로(/) 접속 시 게시판 리스트(/posts)로 리다이렉트
        registry.addRedirectViewController("/", "/posts");
    }
}
