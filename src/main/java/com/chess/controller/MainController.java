package com.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.chess.service.PlayerService;

@Controller
public class MainController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/")
    public String landing() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("activeGames", playerService.getActiveGames(username));
        }
        return "home";
    }
}
