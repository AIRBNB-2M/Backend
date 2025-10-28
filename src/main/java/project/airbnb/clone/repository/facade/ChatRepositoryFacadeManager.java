package project.airbnb.clone.repository.facade;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.entity.chat.ChatMessage;
import project.airbnb.clone.entity.chat.ChatParticipant;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.repository.jpa.ChatMessageRepository;
import project.airbnb.clone.repository.jpa.ChatParticipantRepository;
import project.airbnb.clone.repository.jpa.ChatRoomRepository;
import project.airbnb.clone.repository.query.ChatMessageQueryRepository;
import project.airbnb.clone.repository.query.ChatRoomQueryRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatRepositoryFacadeManager {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageQueryRepository chatMessageQueryRepository;

    public Optional<ChatRoom> findChatRoomByGuestsId(Long otherGuestId, Long creatorId) {
        return chatRoomRepository.findByGuestsId(otherGuestId, creatorId);
    }

    public ChatRoom getChatRoomByRoomId(Long roomId) {
        return chatRoomRepository.findById(roomId)
                                 .orElseThrow(() -> new EntityNotFoundException("Chatroom with id " + roomId + "cannot be found"));
    }

    public List<ChatParticipant> findParticipantsByChatRoom(ChatRoom chatRoom) {
        return chatParticipantRepository.findByChatRoom(chatRoom);
    }

    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessageResDto> getMessages(Long lastMessageId, Long roomId, int pageSize) {
        return chatMessageQueryRepository.getMessages(lastMessageId, roomId, pageSize);
    }

    public ChatMessage getChatMessageById(Long id) {
        return chatMessageRepository.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException("ChatMessage with id: " + id + "cannot be found"));
    }

    public int updateCustomName(String customName, ChatRoom chatRoom, Long id) {
        return chatParticipantRepository.updateCustomName(customName, chatRoom, id);
    }

    public void markLatestMessageAsRead(Long roomId, ChatParticipant chatParticipant) {
        chatMessageRepository.findFirstByChatRoomIdOrderByIdDesc(roomId)
                             .ifPresent(chatParticipant::updateLastReadMessage);
    }

    public List<ChatRoomResDto> findChatRoomsByGuestId(Long guestId) {
        return chatRoomQueryRepository.findChatRooms(guestId);
    }

    public Optional<ChatParticipant> findByChatRoomIdAndGuestId(Long roomId, Long guestId) {
        return chatParticipantRepository.findByChatRoomIdAndGuestId(roomId, guestId);
    }

    public ChatRoom saveChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public void saveChatParticipants(List<ChatParticipant> participants) {
        chatParticipantRepository.saveAll(participants);
    }

    public ChatRoomResDto getChatRoomInfo(Long otherGuestId, Long creatorId, ChatRoom chatRoom) {
        return chatRoomQueryRepository.findChatRoomInfo(otherGuestId, creatorId, chatRoom)
                                      .orElseThrow(() -> new EntityNotFoundException("Chatroom with guests id " + otherGuestId + " and " + creatorId + " cannot be found"));
    }
}
