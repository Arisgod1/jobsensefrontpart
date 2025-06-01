package com.tmd.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {


    private final ChatClient chatClient;
    @RequestMapping("/chat")
    public String chat(String prompt){
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
