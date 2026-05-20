package com.maxin.config;

import com.maxin.constant.SystemConstant;
import com.maxin.utils.AiChatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiAssistantConfiguration {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory memory, AiChatUtils chatTools) {
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(memory).build();
        return ChatClient.builder(model)
                .defaultSystem(SystemConstant.CHAT_SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor(), chatMemoryAdvisor)
                .defaultTools(chatTools)
                .build();
    }
}
