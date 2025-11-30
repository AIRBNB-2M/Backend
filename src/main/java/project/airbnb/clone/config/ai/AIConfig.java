package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
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
                                      .maxMessages(10)
                                      .chatMemoryRepository(jdbcChatMemoryRepository)
                                      .build();
    }

    @Bean
    public ChatMemory anonymousChatMemory() {
        return MessageWindowChatMemory.builder()
                                      .maxMessages(10)
                                      .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                      .build();
    }

    @Bean
    public MessageChatMemoryAdvisor loginMemoryAdvisor(ChatMemory loginChatMemory) {
        return MessageChatMemoryAdvisor.builder(loginChatMemory).build();
    }

    @Bean
    public MessageChatMemoryAdvisor anonymousMemoryAdvisor(ChatMemory anonymousChatMemory) {
        return MessageChatMemoryAdvisor.builder(anonymousChatMemory).build();
    }
}