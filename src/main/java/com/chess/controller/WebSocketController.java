package com.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.chess.service.game.GameService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
public class WebSocketController {
    
    @Autowired
    private GameService gameService;

    //Sends back a response to all clients subscribed to /topic/greetings
    @MessageMapping("/hello")  // Client sends to /app/hello
    @SendTo("/topic/greetings")  // Server broadcasts to /topic/greetings
    public String greeting(String message) throws Exception {
        System.out.println("Received message: " + message);
        return "Server received: " + message;
    }

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(String messageJson) {
        // Parse the JSON message
        JsonObject jsonObject = JsonParser.parseString(messageJson).getAsJsonObject();
        String content = jsonObject.get("content").getAsString();
        String timestamp = jsonObject.get("timestamp").getAsString();
        
        return String.format("[%s] %s", timestamp, content);
    }

    @MessageMapping("/quickplay/join")
    public void joinMatchmaking(String sessionId) {
        System.out.println("Player " + sessionId + " joined matchmaking queue");
        gameService.addPlayerToQueue(sessionId);
    }

    @MessageMapping("/quickplay/leave")
    public void leaveMatchmaking(String sessionId) {
        System.out.println("Player " + sessionId + " left matchmaking queue");
        gameService.removePlayerFromQueue(sessionId);
    }

    @MessageMapping("/game/move")
    public void handleMove(String moveJson) {
        JsonObject jsonObject = JsonParser.parseString(moveJson).getAsJsonObject();
        String sessionId = jsonObject.get("sessionId").getAsString();
        int sourceCoordinate = jsonObject.get("sourceCoordinate").getAsInt();
        int targetCoordinate = jsonObject.get("targetCoordinate").getAsInt();
        
        // Get the game for this player
        gameService.handleWebSocketMove(sessionId, sourceCoordinate, targetCoordinate);
    }
}