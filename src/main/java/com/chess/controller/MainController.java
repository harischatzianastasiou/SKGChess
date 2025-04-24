package com.chess.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chess.dto.rest.response.GameDTO;
import com.chess.model.entity.Game;
import com.chess.model.entity.User;
import com.chess.service.GameService;
import com.chess.service.UserService;


@Controller // GET requests to /req/login and /req/signup return HTML templates AS @Controller is for views
public class MainController {

    private final GameService gameService;
    private final UserService userService;

    public MainController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
    
    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @GetMapping("/game")
    public String game(){
        return "game";
    }

    @GetMapping(value = "/games/{gameId}")
    public String getGame(@PathVariable String gameId, Model model) {
        // Get the game data from service
        Game game = gameService.getGameById(gameId);
        GameDTO gameDTO = GameDTO.fromGame(game);
        
        // Get current user information
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);
        
        // Add all necessary attributes to the model
        model.addAttribute("gameId", gameDTO.getId());
        model.addAttribute("whitePlayerId", gameDTO.getWhitePlayerId());
        model.addAttribute("blackPlayerId", gameDTO.getBlackPlayerId());
        model.addAttribute("whitePlayerUsername", gameDTO.getWhitePlayerUsername());
        model.addAttribute("blackPlayerUsername", gameDTO.getBlackPlayerUsername());
        model.addAttribute("userId", currentUser.getId());
        model.addAttribute("username", username);
        
        // Return the template name
        return "game";
    }

    @GetMapping("/index")
    public String home(){
        return "index";
    }
    
    @GetMapping("/")
    public String root(){
        return "redirect:/index";
    }

    @GetMapping("/about")
    public String about(){
        return "about";
    }
}

// Get requests to /req/login and /req/signup return HTML templates AS @Controller is for views
// Post requests to /req/signup return JSON responses AS @RestController is for REST endpoints
