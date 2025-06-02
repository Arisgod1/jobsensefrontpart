package com.tmd.ai.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class InMemoryChatHistoryRespository implements ChatHistoryRepository{
    private final Map<String,List<String>> chatHistory = new HashMap<>();
    @Override
    public void save(String type, String chatId) {

        List<String> chatIds = chatHistory.computeIfAbsent(type, k -> new ArrayList<>());

        if(chatIds.contains(chatId)){
            return;
        }
        chatIds.add(chatId);
    }

    @Override
    public List<String> getChatIds(String type) {
//        List<String> chatIds = chatHistory.get(type);
//        return chatIds == null ? List.of() : chatIds;
        return chatHistory.getOrDefault(type, List.of());//返回会话id和
    }
}
