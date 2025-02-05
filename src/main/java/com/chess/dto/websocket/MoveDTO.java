package com.chess.dto.websocket;

import com.chess.core.moves.Move;

import lombok.Data;

@Data
public class MoveDTO {
    private String gameId;
    private String userId;
    private Move move;
}
