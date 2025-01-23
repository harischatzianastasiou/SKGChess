package com.chess.test.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import  com.chess.core.Alliance;

class AllianceTest {
    
    @Test
    void testWhiteAlliance() {
        Alliance white = Alliance.WHITE;
        assertEquals(-1, white.getMovingDirection());
        assertTrue(white.isWhite());
        assertFalse(white.isBlack());
        assertEquals(Alliance.BLACK, white.getOpposite());
    }
    
    @Test
    void testBlackAlliance() {
        Alliance black = Alliance.BLACK;
        assertEquals(1, black.getMovingDirection());
        assertFalse(black.isWhite());
        assertTrue(black.isBlack());
        assertEquals(Alliance.WHITE, black.getOpposite());
    }
    
    @Test
    void testOppositeAlliances() {
        assertEquals(Alliance.BLACK, Alliance.WHITE.getOpposite());
        assertEquals(Alliance.WHITE, Alliance.BLACK.getOpposite());
        assertEquals(Alliance.WHITE, Alliance.BLACK.getOpposite().getOpposite().getOpposite());
    }
} 