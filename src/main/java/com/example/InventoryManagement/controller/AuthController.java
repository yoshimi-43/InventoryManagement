package com.example.InventoryManagement.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.InventoryManagement.model.User;
import com.example.InventoryManagement.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) return "register";
        userService.register(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}