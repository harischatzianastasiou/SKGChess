package com.chess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chess.dto.rest.response.GameDTO;
import com.chess.model.entity.Game;
import com.chess.service.GameService;


@Controller // GET requests to /req/login and /req/signup return HTML templates AS @Controller is for views
public class MainController {

    private final GameService gameService;

    public MainController(GameService gameService) {
        this.gameService = gameService;
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
        // Add the game data to the model so it's available in the template
        model.addAttribute("game", GameDTO.fromGame(game));
        // Return the template name (Spring will look for game.html in templates folder)
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
}

// Get requests to /req/login and /req/signup return HTML templates AS @Controller is for views
// Post requests to /req/signup return JSON responses AS @RestController is for REST endpoints
