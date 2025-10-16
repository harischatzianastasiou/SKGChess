package com.chess.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.servlet.http.HttpServletRequest;

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

    // Language-based game route
    @GetMapping("/{lang}/game")
    public String gameWithLanguage(@PathVariable String lang){
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr/game";
        }
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
            return "redirect:/gr?error=unauthorized";
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

    // Language-based game route with gameId
    @GetMapping(value = "/{lang}/games/{gameId}")
    public String getGameWithLanguage(@PathVariable String lang, @PathVariable String gameId, Model model) {
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr/games/" + gameId;
        }
        
        // Add language to model for template use
        model.addAttribute("currentLanguage", lang);
        
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
            // Redirect unauthorized users to the home page with language
            return "redirect:/" + lang + "?error=unauthorized";
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

    // New language-based routes
    @GetMapping("/{lang}")
    public String homeWithLanguage(@PathVariable String lang, Model model){
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr";
        }
        
        // Add language to model for template use
        model.addAttribute("currentLanguage", lang);
        
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

    // Handle /{lang}/index routes to avoid static resource conflicts
    @GetMapping("/{lang}/index")
    public String homeWithLanguageAndIndex(@PathVariable String lang, Model model){
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr";
        }
        
        // Add language to model for template use
        model.addAttribute("currentLanguage", lang);
        
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
    public String root(HttpServletRequest request){
        // Get the Accept-Language header from the request
        String acceptLanguage = request.getHeader("Accept-Language");
        
        // Default to Greek if no language header is present
        String preferredLanguage = "gr";
        
        // Check if Accept-Language header exists and contains English preference
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            // Check if English is preferred (en, en-US, en-GB, etc.)
            if (acceptLanguage.toLowerCase().startsWith("en")) {
                preferredLanguage = "en";
            }
            // Greek is the default, so we don't need to check for 'gr' explicitly
        }
        
        // Redirect to the detected or default language
        return "redirect:/" + preferredLanguage;
    }


    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }


    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }

    @GetMapping("/security-policy")
    public String securityPolicy() {
        return "security-policy";
    }

    // Language-based routes for legal pages
    @GetMapping("/{lang}/privacy-policy")
    public String privacyPolicyWithLanguage(@PathVariable String lang) {
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr/privacy-policy";
        }
        return "privacy-policy";
    }

    @GetMapping("/{lang}/terms-of-use")
    public String termsOfUseWithLanguage(@PathVariable String lang) {
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr/terms-of-use";
        }
        return "terms-of-use";
    }

    @GetMapping("/{lang}/security-policy")
    public String securityPolicyWithLanguage(@PathVariable String lang) {
        // Validate language parameter - only allow 'en' or 'gr'
        if (!"en".equals(lang) && !"gr".equals(lang)) {
            // Redirect to default language (Greek) if invalid language
            return "redirect:/gr/security-policy";
        }
        return "security-policy";
    }
}

// Get requests to /req/login and /req/signup return HTML templates AS @Controller is for views
// Post requests to /req/signup return JSON responses AS @RestController is for REST endpoints
