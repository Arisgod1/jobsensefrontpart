package com.tmd.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory(){
        return new InMemoryChatMemory();
    }
    @Bean
    public ChatClient chatClient(OllamaChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字叫张鸿昊,请以张鸿昊的身份和语气回答问题")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)

                )
                .build();
    }

}
