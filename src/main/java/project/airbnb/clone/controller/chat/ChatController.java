package project.airbnb.clone.controller.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.chat.*;
import project.airbnb.clone.service.chat.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/requests")
    public ResponseEntity<RequestChatResDto> requestChat(@Valid @RequestBody RequestChatReqDto requestChatReqDto,
                                                         @CurrentMemberId Long senderId) {
        RequestChatResDto response = chatService.requestChat(requestChatReqDto.receiverId(), senderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ChatRoomResDto> acceptRequestChat(@PathVariable("requestId") String requestId,
                                                            @CurrentMemberId Long memberId) {
        ChatRoomResDto response = chatService.acceptRequestChat(requestId, memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequestChat(@PathVariable("requestId") String requestId,
                                               @CurrentMemberId Long memberId) {
        chatService.rejectRequestChat(requestId, memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<RequestChatResDto>> getReceivedChatRequests(@CurrentMemberId Long memberId) {
        List<RequestChatResDto> response = chatService.getReceivedChatRequests(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<RequestChatResDto>> getSentChatRequests(@CurrentMemberId Long memberId) {
        List<RequestChatResDto> response = chatService.getSentChatRequests(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResDto>> getChatRooms(@CurrentMemberId Long memberId) {
        List<ChatRoomResDto> response = chatService.getChatRooms(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessagesResDto> getMessageHistories(@RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
                                                                  @RequestParam("size") int pageSize,
                                                                  @PathVariable("roomId") Long roomId,
                                                                  @CurrentMemberId Long memberId) {
        ChatMessagesResDto response = chatService.getMessageHistories(lastMessageId, roomId, pageSize, memberId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{roomId}/name")
    public ResponseEntity<?> updateChatRoomName(@Valid @RequestBody UpdateChatRoomNameReqDto reqDto,
                                                @PathVariable("roomId") Long roomId,
                                                @CurrentMemberId Long memberId) {
        ChatRoomResDto response = chatService.updateChatRoomName(reqDto.customName(), reqDto.otherMemberId(), memberId, roomId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<?> leaveChatRoom(@PathVariable("roomId") Long roomId,
                                           @RequestBody LeaveChatRoomReqDto reqDto,
                                           @CurrentMemberId Long memberId) {
        chatService.leaveChatRoom(roomId, memberId, reqDto.isActive());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roomId}/read")
    public ResponseEntity<?> markChatRoomAsRead(@PathVariable("roomId") Long roomId,
                                                @CurrentMemberId Long memberId) {
        chatService.markChatRoomAsRead(roomId, memberId);
        return ResponseEntity.ok().build();
    }
}
