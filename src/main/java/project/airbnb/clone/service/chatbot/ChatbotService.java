package project.airbnb.clone.service.chatbot;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import project.airbnb.clone.dto.chatbot.ChatbotHistoryResDto;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient chatClient;
    private final ChatMemory loginChatMemory;
    private final ChatMemory anonymousChatMemory;
    private final MessageChatMemoryAdvisor loginMemoryAdvisor;
    private final MessageChatMemoryAdvisor anonymousMemoryAdvisor;

    public Flux<String> postMessage(Long memberId, String message, HttpSession session) {
        boolean isLogin = memberId != null;

        String conversationId = isLogin ? memberId.toString() : getConversationId(session);

        MessageChatMemoryAdvisor advisor = isLogin ? loginMemoryAdvisor : anonymousMemoryAdvisor;

        return chatClient.prompt()
                         .user(message)
                         .advisors(adv -> adv
                                 .advisors(advisor)
                                 .param(ChatMemory.CONVERSATION_ID, conversationId)
                         )
                         .stream()
                         .content();
    }

    public List<ChatbotHistoryResDto> getMessages(Long memberId, HttpSession session) {
        boolean isLogin = memberId != null;

        String conversationId = isLogin ? memberId.toString() : getConversationId(session);

        ChatMemory chatMemory = isLogin ? loginChatMemory : anonymousChatMemory;

        return chatMemory.get(conversationId)
                         .stream()
                         .map(message -> {
                             String content = message.getText();
                             MessageType messageType = message.getMessageType();
                             return new ChatbotHistoryResDto(content, messageType);
                         })
                         .toList();
    }

    private String getConversationId(HttpSession session) {
        String sessionKey = "conversationId";
        String conversationId = (String) session.getAttribute(sessionKey);

        if (!StringUtils.hasText(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            session.setAttribute(sessionKey, conversationId);
        }

        return conversationId;
    }
}
