package project.airbnb.clone.config.ai;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomMessageChatMemoryAdvisor implements BaseChatMemoryAdvisor {

    private final ChatMemory chatMemory;
    private final ChatbotHistoryMemory chatbotHistoryMemory; // 추가

    private final String defaultConversationId;
    private final int order;
    private final Scheduler scheduler;

    private CustomMessageChatMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order,
                                           Scheduler scheduler, ChatbotHistoryMemory chatbotHistoryMemory) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.notNull(chatbotHistoryMemory, "chatbotHistoryMemory cannot be null");
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
        this.chatbotHistoryMemory = chatbotHistoryMemory;
    }

    public List<ChatbotHistoryDto> getMessages(String conversationId) {
        return chatbotHistoryMemory.getMessages(conversationId);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);

        // 1. Retrieve the chat memory for the current conversation.
        List<Message> memoryMessages = this.chatMemory.get(conversationId);

        // 2. Advise the request messages list.
        List<Message> processedMessages = new ArrayList<>(memoryMessages);
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());

        // 3. Create a new request with the advised messages.
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                                                                        .prompt(chatClientRequest.prompt().mutate()
                                                                                                 .messages(processedMessages)
                                                                                                 .build())
                                                                        .build();

        // 4. Add the new user message to the conversation memory.
        UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
        this.chatMemory.add(conversationId, userMessage);

        // 추가
        chatbotHistoryMemory.save(conversationId, userMessage, null);

        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        List<Message> assistantMessages = new ArrayList<>();
        if (chatClientResponse.chatResponse() != null) {
            assistantMessages = chatClientResponse.chatResponse()
                                                  .getResults()
                                                  .stream()
                                                  .map(g -> (Message) g.getOutput())
                                                  .toList();
        }
        String conversationId = this.getConversationId(chatClientResponse.context(), this.defaultConversationId);
        this.chatMemory.add(conversationId, assistantMessages);

        // 추가
        Map<String, Object> context = chatClientResponse.context();
        Map<String, Object> metadata = (Map<String, Object>) context.get("metadata");
        assistantMessages.forEach(assistantMessage -> chatbotHistoryMemory.save(conversationId, assistantMessage, metadata));

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        // Get the scheduler from BaseAdvisor
        Scheduler scheduler = this.getScheduler();

        // Process the request with the before method
        return Mono.just(chatClientRequest)
                   .publishOn(scheduler)
                   .map(request -> this.before(request, streamAdvisorChain))
                   .flatMapMany(streamAdvisorChain::nextStream)
                   .transform(flux -> new ChatClientMessageAggregator().aggregateChatClientResponse(flux,
                           response -> this.after(response, streamAdvisorChain)));
    }

    public static CustomMessageChatMemoryAdvisor.Builder builder(ChatMemory chatMemory) {
        return new CustomMessageChatMemoryAdvisor.Builder(chatMemory);
    }

    public static final class Builder {

        private String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;

        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

        private ChatMemory chatMemory;
        private ChatbotHistoryMemory chatbotHistoryMemory; // 추가

        private Builder(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
        }

        /**
         * Set the conversation id.
         *
         * @param conversationId the conversation id
         * @return the builder
         */
        public CustomMessageChatMemoryAdvisor.Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        /**
         * Set the order.
         *
         * @param order the order
         * @return the builder
         */
        public CustomMessageChatMemoryAdvisor.Builder order(int order) {
            this.order = order;
            return this;
        }

        public CustomMessageChatMemoryAdvisor.Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        // 추가
        public CustomMessageChatMemoryAdvisor.Builder chatbotHistoryMemory(ChatbotHistoryMemory chatbotHistoryMemory) {
            this.chatbotHistoryMemory = chatbotHistoryMemory;
            return this;
        }

        /**
         * Build the advisor.
         *
         * @return the advisor
         */
        public CustomMessageChatMemoryAdvisor build() {
            // 추가
            if (this.chatbotHistoryMemory == null) {
                this.chatbotHistoryMemory = new InMemoryChatbotHistoryMemory();
            }
            return new CustomMessageChatMemoryAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler, this.chatbotHistoryMemory);
        }

    }
}
