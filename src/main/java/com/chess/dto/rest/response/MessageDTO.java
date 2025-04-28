package com.chess.dto.rest.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.chess.model.entity.Message;

/**
 * DTO for Message entity.
 * Used for transferring message data between the server and client.
 */
@Getter
@Setter
@ToString
public class MessageDTO {
    private String id;
    private String gameId;
    private String opponentUsername;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
    private String type;

    /**
     * Convert a Message entity to a MessageDTO
     * @param message The Message entity to convert
     * @return The converted MessageDTO
     */
    public static MessageDTO fromMessage(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setGameId(message.getGameId());
        dto.setOpponentUsername(message.getOpponentUsername());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        dto.setType(message.getType());
        return dto;
    }
} 