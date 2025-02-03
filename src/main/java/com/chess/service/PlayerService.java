package com.chess.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chess.exception.PlayerNotFoundException;
import com.chess.model.entity.Player;
import com.chess.repository.PlayerRepository;
import com.chess.model.entity.Game;
import java.util.List;
import java.util.ArrayList;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class PlayerService implements UserDetailsService {

    private PlayerRepository playerRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Player player = playerRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        return org.springframework.security.core.userdetails.User
            .withUsername(player.getUsername())
            .password(player.getPassword())
            .authorities(new ArrayList<SimpleGrantedAuthority>())
            .build();
    }

    public Player registerPlayer(String username, String email, String password) {
        Player player = new Player(username, email);
        player.setPassword(passwordEncoder.encode(password));
        return playerRepository.save(player);
    }

    public Player getPlayerByUsernameOrEmail(String usernameOrEmail) {
        return playerRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    public Player getPlayerById(String id) {
        return playerRepository.findById(id).orElse(null);
    }

    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    public Player getPlayerByEmail(String email) {
        return playerRepository.findByEmail(email)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }

    public Player updatePlayer(Player updatedPlayer) {
        return playerRepository.save(updatedPlayer);
    }

    public void deletePlayer(String id) {
        playerRepository.deleteById(id);
    }

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
