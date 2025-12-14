package project.airbnb.clone.controller.chatbot;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.config.ai.ChatbotHistoryDto;
import project.airbnb.clone.config.ai.ChatbotResponseDto;
import project.airbnb.clone.dto.chatbot.ChatbotReqDto;
import project.airbnb.clone.service.chatbot.ChatbotService;

import java.util.List;

@RestController
@RequestMapping("/api/chat-bot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatbotResponseDto> postMessage(@CurrentMemberId(required = false) Long memberId,
                                                          @RequestBody ChatbotReqDto reqDto,
                                                          HttpSession session) {
        ChatbotResponseDto response = chatbotService.postMessage(memberId, reqDto.message(), session);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ChatbotHistoryDto>> getMessages(@CurrentMemberId(required = false) Long memberId,
                                                               HttpSession session) {
        List<ChatbotHistoryDto> response = chatbotService.getMessages(memberId, session);
        return ResponseEntity.ok(response);
    }
}
