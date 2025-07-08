package com.chess.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.chess.dto.rest.response.GameDTO;
import com.chess.dto.rest.response.UserDTO;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.model.entity.User;
import com.chess.service.GameService;
import com.chess.service.UserService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Controller
@CrossOrigin(origins = {"https://toskaki.gr", "https://www.toskaki.gr", "https://skaki.online", "https://www.skaki.online", "https://toskaki.fly.dev", "http://localhost:8080"}, maxAge = 3600)
public class MainController {

    private final GameService gameService;
    private final UserService userService;

    public MainController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
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
        
        // Check if the current user is either the white or black player
        boolean isAuthorized = gameDTO.getWhitePlayerId().equals(currentUser.getId()) || 
                             (gameDTO.getBlackPlayerId() != null && gameDTO.getBlackPlayerId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            // Redirect unauthorized users to the home page
            return "redirect:/index?error=unauthorized";
        }
        
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
    public String home(Model model){
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                // Get current user
                String username = auth.getName();
                User currentUser = userService.getUserByUsername(username);
                
                // Get all games for the current user
                List<Game> recentGames = gameService.getLast6GamesForUser(currentUser.getId());
                List<Game> lastGame = gameService.getLastGameForUser(currentUser.getId());
                int numOfUserGames = gameService.numOfUserGames(currentUser.getId());

                // Convert to DTOs for the view
                List<GameDTO> recentGameDTOs = recentGames.stream()
                    .map(GameDTO::fromGame)
                    .collect(Collectors.toList());

                // Handle last game conversion safely
                GameDTO lastGameDTO = lastGame != null && !lastGame.isEmpty() ? 
                    GameDTO.fromGame(lastGame.get(0)) : new GameDTO();
                
                // Add to model
                model.addAttribute("checkmateGames", recentGameDTOs);
                model.addAttribute("lastGame", lastGameDTO);
                model.addAttribute("numOfUserGames", numOfUserGames);
                model.addAttribute("userId", currentUser.getId());
                model.addAttribute("user", UserDTO.fromUser(currentUser));
                
            } catch (Exception e) {
                // Log the error but don't let it crash the page
                System.err.println("Error loading all games: " + e.getMessage());
                e.printStackTrace();
                // Add empty list to avoid null pointer in template
                model.addAttribute("checkmateGames", new ArrayList<>());
                model.addAttribute("lastGame", new GameDTO());
                model.addAttribute("numOfUserGames", 0);
            }
        }
        
        return "index";
    }
    
    @GetMapping("/")
    public String root(Model model){
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                // Get current user
                String username = auth.getName();
                User currentUser = userService.getUserByUsername(username);
                
                // Get all games for the current user
                List<Game> recentGames = gameService.getLast6GamesForUser(currentUser.getId());
                List<Game> lastGame = gameService.getLastGameForUser(currentUser.getId());
                int numOfUserGames = gameService.numOfUserGames(currentUser.getId());

                // Convert to DTOs for the view
                List<GameDTO> recentGameDTOs = recentGames.stream()
                    .map(GameDTO::fromGame)
                    .collect(Collectors.toList());

                // Handle last game conversion safely
                GameDTO lastGameDTO = lastGame != null && !lastGame.isEmpty() ? 
                    GameDTO.fromGame(lastGame.get(0)) : new GameDTO();
                
                // Add to model
                model.addAttribute("checkmateGames", recentGameDTOs);
                model.addAttribute("lastGame", lastGameDTO);
                model.addAttribute("numOfUserGames", numOfUserGames);
                model.addAttribute("userId", currentUser.getId());
                model.addAttribute("user", UserDTO.fromUser(currentUser));
            } catch (Exception e) {
                // Log the error but don't let it crash the page
                System.err.println("Error loading all games: " + e.getMessage());
                e.printStackTrace();
                // Add empty list to avoid null pointer in template
                model.addAttribute("checkmateGames", new ArrayList<>());
                model.addAttribute("lastGame", new GameDTO());
                model.addAttribute("numOfUserGames", 0);
            }
        }
        
        // Return the index template directly instead of redirecting
        return "index";
    }

    @GetMapping("/about")
    public String about(){
        return "about";
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}

// Get requests to /req/login and /req/signup return HTML templates AS @Controller is for views
// Post requests to /req/signup return JSON responses AS @RestController is for REST endpoints
