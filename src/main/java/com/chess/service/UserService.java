package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.Game;
import com.chess.model.entity.User;
import com.chess.repository.UserRepository;

@Service

public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            // .authorities(new ArrayList<SimpleGrantedAuthority>())
            .build();

    }

    public User registerUser(User user) {
        user.setRating(800);
        user.setGamesPlayed(0);
        user.setGamesWon(0);
        user.setGamesLost(0);
        user.setGamesDraw(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        return userRepository.save(user);
    }


    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }


    // public Player getPlayerById(String id) {
    //     return playerRepository.findById(id).orElse(null);
    // }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
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

    public List<Game> getAllGames(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getAllGames();
    }


    public List<Game> getActiveGames(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return user.getActiveGames();
    }

}
