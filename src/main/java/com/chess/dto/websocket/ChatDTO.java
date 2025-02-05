package com.chess.dto.websocket;

import lombok.Data;

@Data
public class ChatDTO {
    private String gameId;
    private String message;
    private String sender;
    private Long timestamp;
} 