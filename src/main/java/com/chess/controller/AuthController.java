package com.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chess.dto.request.SignupRequest;
import com.chess.service.PlayerService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("signupRequest")) {
            model.addAttribute("signupRequest", new SignupRequest());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.signupRequest", bindingResult);
            redirectAttributes.addFlashAttribute("signupRequest", signupRequest);
            redirectAttributes.addFlashAttribute("error", "Please correct the errors in the form");
            return "redirect:/signup";
        }

        // Check if passwords match
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("signupRequest", signupRequest);
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/signup";
        }

        try {
            playerService.registerPlayer(
                signupRequest.getUsername(), 
                signupRequest.getEmail(),
                signupRequest.getPassword()
            );
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("signupRequest", signupRequest);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }
} 