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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient openAiChatClient(ChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    @Bean
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        return JdbcChatMemoryRepository.builder()
                                       .jdbcTemplate(jdbcTemplate)
                                       .transactionManager(transactionManager)
                                       .build();
    }

    @Bean
    public InMemoryChatMemoryRepository inMemoryChatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory loginChatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                                      .maxMessages(10)
                                      .chatMemoryRepository(jdbcChatMemoryRepository)
                                      .build();
    }

    @Bean
    public ChatMemory anonymousChatMemory(InMemoryChatMemoryRepository inMemoryChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                                      .maxMessages(10)
                                      .chatMemoryRepository(inMemoryChatMemoryRepository)
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