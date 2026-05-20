package com.maxin.controller;

import com.maxin.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam("message") String message) {
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return Result.success(response);
    }

    @PostMapping("/chat")
    public Result<String> chatPost(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return Result.success(response);
    }
}
