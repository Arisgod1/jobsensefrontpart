package com.tmd.ai.controller;

import com.tmd.ai.constants.SystemConstants;
import com.tmd.ai.entity.vo.PDFVO;
import com.tmd.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class InterviewController {

    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @PostMapping(value = "/interview", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestBody PDFVO pdfVO){
        String prompt = pdfVO.getPrompt();
        String chatId = pdfVO.getChatId();
        //1.保存会话ID
        chatHistoryRepository.save("interview",chatId);
        //2.请求模型
        return chatClient.prompt()
                .system(SystemConstants.PDF_SYSTEM_PROMPT)
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();
    }
}