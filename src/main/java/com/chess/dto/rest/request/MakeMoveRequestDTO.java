package com.chess.dto.rest.request;

import lombok.Data;

@Data
public class MakeMoveRequestDTO {
    private String gameId;
    private int sourceCoordinate;
    private int targetCoordinate;
    private String promotionPieceType; // Optional field for pawn promotion piece selection
}
