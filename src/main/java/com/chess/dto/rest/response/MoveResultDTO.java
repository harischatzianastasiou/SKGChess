package com.chess.dto.rest.response;

import com.chess.core.board.IBoard;
import com.chess.model.entity.Game;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoveResultDTO {
    private GameDTO gameDTO;
    private IBoard board;
}
