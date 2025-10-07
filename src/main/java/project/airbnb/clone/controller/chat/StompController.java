package project.airbnb.clone.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import project.airbnb.clone.dto.chat.ChatMessageReqDto;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.service.chat.ChatService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messageTemplate;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Long roomId, ChatMessageReqDto chatMessageDto) {
        ChatMessageResDto savedChatMessage = chatService.saveChatMessage(roomId, chatMessageDto);
        messageTemplate.convertAndSend("/topic/" + roomId, savedChatMessage);
    }
}
