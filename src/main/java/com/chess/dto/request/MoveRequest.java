package com.chess.dto.request;

import com.chess.core.moves.Move;

import lombok.Data;

@Data
public class MoveRequest {
    private String gameId;
    private String playerId;
    private Move move;
}
