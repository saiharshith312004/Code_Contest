package com.onboarding.authservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // maps to src/main/resources/templates/login.html
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // maps to src/main/resources/templates/register.html
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard"; // maps to dashboard.html (optional)
    }
}
