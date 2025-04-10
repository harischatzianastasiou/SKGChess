package com.chess.dto.response;

import java.time.LocalDateTime;

import com.chess.model.entity.Game.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Game entity
 * Used to avoid circular references in JSON serialization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    // Game ID
    private String id;
    
    // Player IDs (not the full User objects)
    private String whitePlayerId;
    private String blackPlayerId;
    
    // Player usernames for display
    private String whitePlayerUsername;
    private String blackPlayerUsername;
    
    // Game state
    private String fenPosition;
    private String pgnMoves;
    private GameStatus status;
    private LocalDateTime createdAt;
    
    // Game result
    private String winnerId;
    private String winnerUsername;
    
    // Game statistics
    private int moveCount;
    private String lastMovePgn;
    private boolean isBlackPlayerCastled;
    private boolean isWhitePlayerCastled;
    
    /**
     * Convert a Game entity to a GameDTO
     * @param game The Game entity to convert
     * @return A new GameDTO with data from the Game entity
     */
            //else use GameMapper
            /*@Component
            public class GameMapper {
                public GameDTO toDTO(Game game) {
                    return GameDTO.builder()
                            .id(game.getId())
                            .whitePlayerId(game.getWhitePlayer() != null ? game.getWhitePlayer().getId() : null)
                            // ... other mappings
                            .build();
                }
                
                // Could also include methods for converting collections, or DTO to entity
                public List<GameDTO> toDTOList(List<Game> games) {
                    return games.stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());
                }
            } */
    public static GameDTO fromGame(com.chess.model.entity.Game game) {
        return GameDTO.builder()
                .id(game.getId())
                .whitePlayerId(game.getWhitePlayer() != null ? game.getWhitePlayer().getId() : null)
                .blackPlayerId(game.getBlackPlayer() != null ? game.getBlackPlayer().getId() : null)
                .whitePlayerUsername(game.getWhitePlayer() != null ? game.getWhitePlayer().getUsername() : null)
                .blackPlayerUsername(game.getBlackPlayer() != null ? game.getBlackPlayer().getUsername() : null)
                .fenPosition(game.getFenPosition())
                .pgnMoves(game.getPgnMoves())
                .status(game.getStatus())
                .createdAt(game.getCreatedAt())
                .winnerId(game.getWinner() != null ? game.getWinner().getId() : null)
                .winnerUsername(game.getWinner() != null ? game.getWinner().getUsername() : null)
                .moveCount(game.getMoveCount())
                .lastMovePgn(game.getLastMovePgn())
                .isBlackPlayerCastled(game.isBlackPlayerCastled())
                .isWhitePlayerCastled(game.isWhitePlayerCastled())
                .build();
    }
} 