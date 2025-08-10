package com.onboarding.authservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminPageController {

    @GetMapping("/admin-login")
    public String showAdminLoginPage() {
        return "admin-login";
    }

    @GetMapping("/admin-dashboard")
    public String showAdminDashboard() {
        return "admin-dashboard";
    }
}
