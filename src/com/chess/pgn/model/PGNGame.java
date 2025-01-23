package com.chess.pgn.model;

import java.sql.Date;

public class PGNGame {
    private Long id;
    private String event;
    private String site;
    private Date date;
    private String round;
    private String white;
    private String black;
    private String result;
    private String moves;
    private String eco;  // ECO code for the opening

    public PGNGame() {}

    public PGNGame(String event, String site, Date date, String round, 
                  String white, String black, String result, String moves, String eco) {
        this.event = event;
        this.site = site;
        this.date = date;
        this.round = round;
        this.white = white;
        this.black = black;
        this.result = result;
        this.moves = moves;
        this.eco = eco;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }

    public String getWhite() { return white; }
    public void setWhite(String white) { this.white = white; }

    public String getBlack() { return black; }
    public void setBlack(String black) { this.black = black; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getMoves() { return moves; }
    public void setMoves(String moves) { this.moves = moves; }

    public String getEco() { return eco; }
    public void setEco(String eco) { this.eco = eco; }

    @Override
    public String toString() {
        return String.format("%s vs %s, %s (%s)", white, black, event, date);
    }
} 