package com.tmd.ai.entity.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> role = "user";
            case ASSISTANT -> role = "assistant";
            default -> role = "";
        }
        this.content = message.getText();
    }
}
