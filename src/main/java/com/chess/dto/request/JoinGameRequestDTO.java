package com.chess.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinGameRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Game ID is required")
    private String gameId;
}
