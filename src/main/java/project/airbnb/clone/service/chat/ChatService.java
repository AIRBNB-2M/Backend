package project.airbnb.clone.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.chat.ParticipantLeftException;
import project.airbnb.clone.dto.chat.ChatMessageReqDto;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.dto.chat.ChatMessagesResDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.chat.ChatMessage;
import project.airbnb.clone.entity.chat.ChatParticipant;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.repository.facade.ChatRepositoryFacadeManager;
import project.airbnb.clone.repository.jpa.GuestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final GuestRepository guestRepository;
    private final ChatRepositoryFacadeManager chatRepositoryFacade;

    @Transactional
    public ChatRoomResDto createOrGetChatRoom(Long otherGuestId, Long creatorId) {
        if (otherGuestId.equals(creatorId)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다.");
        }

        ChatRoom chatRoom = chatRepositoryFacade.findChatRoomByGuestsId(otherGuestId, creatorId)
                                                .map(existingRoom -> {
                                                    reactiveIfHasLeft(existingRoom.getId(), creatorId);
                                                    return existingRoom;
                                                })
                                                .orElseGet(() -> createNewChatRoom(otherGuestId, creatorId));

        return chatRepositoryFacade.getChatRoomInfo(otherGuestId, creatorId, chatRoom);
    }

    @Transactional
    public ChatMessageResDto saveChatMessage(Long roomId, ChatMessageReqDto chatMessageDto) {
        ChatRoom chatRoom = chatRepositoryFacade.getChatRoomByRoomId(roomId);
        List<ChatParticipant> participants = chatRepositoryFacade.findParticipantsByChatRoom(chatRoom);

        for (ChatParticipant participant : participants) {
            if (participant.hasLeft()) {
                throw new ParticipantLeftException("Cannot send message: one of the participants has left the chat, left guestId: " + participant.getGuest().getId());
            }
        }

        Long senderId = chatMessageDto.senderId();
        String content = chatMessageDto.content();

        Guest writer = getGuestById(senderId);
        ChatMessage message = chatRepositoryFacade.saveChatMessage(ChatMessage.builder()
                                                                              .chatRoom(chatRoom)
                                                                              .writer(writer)
                                                                              .content(content)
                                                                              .build());
        ChatParticipant chatParticipant = participants.stream()
                                                      .filter(participant -> participant.getGuest().getId().equals(writer.getId()))
                                                      .findFirst()
                                                      .orElseThrow(() -> new EntityNotFoundException("Writer " + writer.getId() + " not found in participants"));
        chatParticipant.updateLastReadMessage(message);

        return ChatMessageResDto.builder()
                                .messageId(message.getId())
                                .roomId(roomId)
                                .senderId(writer.getId())
                                .senderName(writer.getName())
                                .content(message.getContent())
                                .timestamp(message.getCreatedAt())
                                .build();
    }

    @Transactional
    public ChatMessagesResDto getMessageHistories(Long lastMessageId, Long roomId, int pageSize, Long guestId) {
        List<ChatMessageResDto> messages = chatRepositoryFacade.getMessages(lastMessageId, roomId, pageSize);

        if (lastMessageId == null && !messages.isEmpty()) {
            Long lastId = messages.get(0).messageId();
            ChatParticipant chatParticipant = getChatParticipant(roomId, guestId);
            ChatMessage lastMessage = chatRepositoryFacade.getChatMessageById(lastId);
            chatParticipant.updateLastReadMessage(lastMessage);
        }

        boolean hasMore = messages.size() > pageSize;

        if (hasMore) {
            messages.remove(messages.size() - 1);
        }

        return new ChatMessagesResDto(messages, hasMore);
    }

    @Transactional
    public ChatRoomResDto updateChatRoomName(String customName, Long otherGuestId, Long myId, Long roomId) {
        ChatRoom chatRoom = chatRepositoryFacade.getChatRoomByRoomId(roomId);

        int updated = chatRepositoryFacade.updateCustomName(customName, chatRoom, myId);
        if (updated == 0) {
            throw new EntityNotFoundException("ChatParticipant with roomId: " + roomId + " and guestId: " + myId + "cannot be found");
        }

        return chatRepositoryFacade.getChatRoomInfo(otherGuestId, myId, chatRoom);
    }

    @Transactional
    public void leaveChatRoom(Long roomId, Long guestId, Boolean active) {
        ChatParticipant chatParticipant = getChatParticipant(roomId, guestId);
        chatParticipant.leave();
        //TODO : 상대방에게 채팅방 나감을 알리는 이벤트 발행

        if (active) {
            chatRepositoryFacade.markLatestMessageAsRead(roomId, chatParticipant);
        }
    }

    public List<ChatRoomResDto> getChatRooms(Long guestId) {
        return chatRepositoryFacade.findChatRoomsByGuestId(guestId);
    }

    public boolean isChatRoomParticipant(Long roomId, Long guestId) {
        return chatRepositoryFacade.findByChatRoomIdAndGuestId(roomId, guestId)
                                   .map(ChatParticipant::isActiveParticipant)
                                   .orElse(false);
    }

    private ChatRoom createNewChatRoom(Long otherGuestId, Long creatorId) {
        Guest otherGuest = getGuestById(otherGuestId);
        Guest creatorGuest = getGuestById(creatorId);

        ChatRoom chatRoom = chatRepositoryFacade.saveChatRoom(new ChatRoom());

        List<ChatParticipant> newParticipants = List.of(
                ChatParticipant.builder()
                               .chatRoom(chatRoom)
                               .guest(otherGuest)
                               .isCreator(false)
                               .customRoomName(creatorGuest.getName() + "님과의 대화")
                               .build(),
                ChatParticipant.builder()
                               .chatRoom(chatRoom)
                               .guest(creatorGuest)
                               .isCreator(true)
                               .customRoomName(otherGuest.getName() + "님과의 대화")
                               .build()
        );

        chatRepositoryFacade.saveChatParticipants(newParticipants);
        return chatRoom;
    }

    private void reactiveIfHasLeft(Long roomId, Long guestId) {
        ChatParticipant chatParticipant = getChatParticipant(roomId, guestId);
        if (chatParticipant.hasLeft()) {
            chatParticipant.rejoin();
        }
    }

    private Guest getGuestById(Long guestId) {
        return guestRepository.findById(guestId)
                              .orElseThrow(() -> new EntityNotFoundException("Guest with id " + guestId + "cannot be found"));
    }

    private ChatParticipant getChatParticipant(Long roomId, Long guestId) {
        return chatRepositoryFacade.findByChatRoomIdAndGuestId(roomId, guestId)
                                   .orElseThrow(() -> new EntityNotFoundException("ChatParticipant with roomId: " + roomId + " and guestId: " + guestId + "cannot be found"));
    }
}
