package project.airbnb.clone.controller.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.chat.ChatMessagesResDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.dto.chat.CreateChatRoomReqDto;
import project.airbnb.clone.dto.chat.LeaveChatRoomReqDto;
import project.airbnb.clone.dto.chat.RequestChatReqDto;
import project.airbnb.clone.dto.chat.RequestChatResDto;
import project.airbnb.clone.dto.chat.UpdateChatRoomNameReqDto;
import project.airbnb.clone.service.chat.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/requests")
    public ResponseEntity<RequestChatResDto> requestChat(@Valid @RequestBody RequestChatReqDto requestChatReqDto,
                                                         @CurrentGuestId Long senderId) {
        RequestChatResDto response = chatService.requestChat(requestChatReqDto.receiverId(), senderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<RequestChatResDto>> getReceivedChatRequests(@CurrentGuestId Long guestId) {
        List<RequestChatResDto> response = chatService.getReceivedChatRequests(guestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<RequestChatResDto>> getSentChatRequests(@CurrentGuestId Long guestId) {
        List<RequestChatResDto> response = chatService.getSentChatRequests(guestId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResDto> createOrGetChatRoom(@RequestBody CreateChatRoomReqDto reqDto,
                                                              @CurrentGuestId Long guestId) {
        ChatRoomResDto response = chatService.createOrGetChatRoom(reqDto.otherGuestId(), guestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResDto>> getChatRooms(@CurrentGuestId Long guestId) {
        List<ChatRoomResDto> response = chatService.getChatRooms(guestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessagesResDto> getMessageHistories(@RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
                                                                  @RequestParam("size") int pageSize,
                                                                  @PathVariable("roomId") Long roomId,
                                                                  @CurrentGuestId Long guestId) {
        ChatMessagesResDto response = chatService.getMessageHistories(lastMessageId, roomId, pageSize, guestId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{roomId}/name")
    public ResponseEntity<?> updateChatRoomName(@Valid @RequestBody UpdateChatRoomNameReqDto reqDto,
                                                @PathVariable("roomId") Long roomId,
                                                @CurrentGuestId Long guestId) {
        ChatRoomResDto response = chatService.updateChatRoomName(reqDto.customName(), reqDto.otherGuestId(), guestId, roomId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<?> leaveChatRoom(@PathVariable("roomId") Long roomId,
                                           @RequestBody LeaveChatRoomReqDto reqDto,
                                           @CurrentGuestId Long guestId) {
        chatService.leaveChatRoom(roomId, guestId, reqDto.isActive());
        return ResponseEntity.ok().build();
    }
}
