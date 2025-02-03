package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chess.exception.PlayerNotFoundException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Player;
import com.chess.repository.PlayerRepository;

@Service
public class PlayerService implements UserDetailsService {

    private PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        return org.springframework.security.core.userdetails.User
            .withUsername(player.getUsername())
            .password(player.getPassword())
            // .authorities(new ArrayList<SimpleGrantedAuthority>())
            .build();
    }

    public Player registerPlayer(Player player) {
        player.setRating(800);
        player.setGamesPlayed(0);
        player.setGamesWon(0);
        player.setGamesLost(0);
        player.setGamesDraw(0);
        player.setCreatedAt(LocalDateTime.now());
        player.setLastLogin(LocalDateTime.now());
        return playerRepository.save(player);
    }

    public Player getPlayerByUsernameOrEmail(String usernameOrEmail) {
        return playerRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    // public Player getPlayerById(String id) {
    //     return playerRepository.findById(id).orElse(null);
    // }

    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    // public Player getPlayerByEmail(String email) {
    //     return playerRepository.findByEmail(email)
    //         .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    // }

    // public Player updatePlayer(Player updatedPlayer) {
    //     return playerRepository.save(updatedPlayer);
    // }

    // public void deletePlayer(String id) {
    //     playerRepository.deleteById(id);
    // }

    public List<Game> getAllGames(String playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        return player.getAllGames();
    }

    public List<Game> getActiveGames(String username) {
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found with username: " + username));
        return player.getActiveGames();
    }
}
