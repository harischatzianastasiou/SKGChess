package com.chess.api.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChessWebSocketController {


    //Sends back a response to all clients subscribed to /topic/greetings
    @MessageMapping("/hello")  // Client sends to /app/hello
    @SendTo("/topic/greetings")  // Server broadcasts to /topic/greetings
    public String greeting(String message) throws Exception {
        System.out.println("Received message: " + message);
        return "Server received: " + message;
    }
}