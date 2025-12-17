package com.mycom.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	
	// index 메인화면
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    // login 페이지 
    @GetMapping("/login")
    public String loginPage() {
        return "login.html"; 
    }
    
    // 회원가입 페이지
    @GetMapping("/register") 
    public String signupPage() {
        return "register.html"; 
    }
}