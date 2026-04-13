package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "안녕하세요! 로컬 API 서버가 정상적으로 동작하고 있습니다.";
    }
}