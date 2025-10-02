package project.airbnb.clone.controller.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.chat.CreateChatRoomReqDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.service.chat.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResDto> createOrGetChatRoom(@RequestBody CreateChatRoomReqDto reqDto,
                                                              @CurrentGuestId Long guestId) {
        ChatRoomResDto response = chatService.createOrGetChatRoom(reqDto.otherGuestId(), guestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getChatRooms(@CurrentGuestId Long guestId) {
        List<ChatRoomResDto> response = chatService.getChatRooms(guestId);
        return ResponseEntity.ok(response);
    }
}
