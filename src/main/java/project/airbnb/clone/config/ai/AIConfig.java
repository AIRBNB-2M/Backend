package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient openAiChatClient(ChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    @Bean
    public ChatMemory loginChatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                                      .chatMemoryRepository(jdbcChatMemoryRepository)
                                      .build();
    }

    @Bean
    public ChatMemory anonymousChatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    public CustomMessageChatMemoryAdvisor loginMemoryAdvisor(ChatMemory loginChatMemory, ChatbotHistoryMemory chatbotHistoryMemory) {
        return CustomMessageChatMemoryAdvisor.builder(loginChatMemory)
                                             .chatbotHistoryMemory(chatbotHistoryMemory)
                                             .build();
    }

    @Bean
    public CustomMessageChatMemoryAdvisor anonymousMemoryAdvisor(ChatMemory anonymousChatMemory) {
        return CustomMessageChatMemoryAdvisor.builder(anonymousChatMemory).build();
    }
}