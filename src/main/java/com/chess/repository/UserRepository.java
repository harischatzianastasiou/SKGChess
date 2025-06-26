package com.chess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import com.chess.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    // Add pessimistic locking for user lookup
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithLock(@Param("username") String username);

    // Check if user has any active games
    @Query("SELECT COUNT(g) > 0 FROM Game g WHERE (g.whitePlayer.id = :userId OR g.blackPlayer.id = :userId) AND g.status = 'IN_PROGRESS'")
    boolean existsActiveGameForUser(@Param("userId") String userId);
    
    /**
     * Search for users by username containing the query (case-insensitive)
     * Excludes a specific user from results
     * @param query The username query to search for
     * @param excludeUsername The username to exclude from results
     * @return List of users matching the query
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) AND u.username != :excludeUsername ORDER BY u.username ASC")
    List<User> findByUsernameContainingIgnoreCaseAndUsernameNot(@Param("query") String query, @Param("excludeUsername") String excludeUsername);
}
