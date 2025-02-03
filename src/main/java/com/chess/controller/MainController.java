package com.chess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // GET requests to /req/login and /req/signup return HTML templates AS @Controller is for views
public class MainController {

    // private final PlayerService playerService;

    // public MainController(PlayerService playerService) {
    //     this.playerService = playerService;
    // }

    @GetMapping("/req/login")
    public String login(){
        return "login";
    }
    
    @GetMapping("/req/signup")
    public String signup(){
        return "signup";
    }

    @GetMapping("/index")
    public String home(){
        return "index";
    }
}

// Get requests to /req/login and /req/signup return HTML templates AS @Controller is for views
// Post requests to /req/signup return JSON responses AS @RestController is for REST endpoints
