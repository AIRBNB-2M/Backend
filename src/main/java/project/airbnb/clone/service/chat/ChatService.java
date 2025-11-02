package project.airbnb.clone.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.events.chat.ChatRequestAcceptedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestCreatedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestRejectedEvent;
import project.airbnb.clone.common.exceptions.chat.AlreadyActiveChatException;
import project.airbnb.clone.common.exceptions.chat.AlreadyRequestException;
import project.airbnb.clone.common.exceptions.chat.ParticipantLeftException;
import project.airbnb.clone.common.exceptions.chat.SameParticipantException;
import project.airbnb.clone.dto.chat.ChatMessageReqDto;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.dto.chat.ChatMessagesResDto;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.dto.chat.RequestChatResDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.chat.ChatMessage;
import project.airbnb.clone.entity.chat.ChatParticipant;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.repository.dto.ChatRequest;
import project.airbnb.clone.repository.facade.ChatRepositoryFacadeManager;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.redis.ChatRequestRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final GuestRepository guestRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatRepositoryFacadeManager chatRepositoryFacade;

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

    @Transactional
    public ChatRoomResDto acceptRequestChat(String requestId, Long receiverId) {
        ChatRequest chatRequest = chatRequestRepository.findById(requestId)
                                                       .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않거나 만료되었습니다. => " + requestId));

        if (!chatRequest.getReceiverId().equals(receiverId)) throw new AccessDeniedException("본인에게 온 요청만 수락할 수 있습니다.");

        chatRequestRepository.delete(chatRequest);

        Long senderId = chatRequest.getSenderId();
        ChatRoom chatRoom = chatRepositoryFacade.findChatRoomByGuestsId(receiverId, senderId)
                                                .map(existingRoom -> {
                                                    reactiveIfHasLeft(existingRoom.getId(), receiverId);
                                                    reactiveIfHasLeft(existingRoom.getId(), senderId);
                                                    return existingRoom;
                                                })
                                                .orElseGet(() -> createNewChatRoom(receiverId, senderId));

        ChatRoomResDto senderChatRoomInfo = chatRepositoryFacade.getChatRoomInfo(senderId, receiverId, chatRoom);
        eventPublisher.publishEvent(new ChatRequestAcceptedEvent(requestId, senderId, senderChatRoomInfo));

        return  chatRepositoryFacade.getChatRoomInfo(receiverId, senderId, chatRoom);
    }

    public void rejectRequestChat(String requestId, Long rejecterId) {
        ChatRequest chatRequest = chatRequestRepository.findById(requestId)
                                                       .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않거나 만료되었습니다. => " + requestId));

        if (!chatRequest.getReceiverId().equals(rejecterId)) throw new AccessDeniedException("본인에게 온 요청만 거절할 수 있습니다.");

        chatRequestRepository.delete(chatRequest);
        eventPublisher.publishEvent(new ChatRequestRejectedEvent(requestId, chatRequest.getSenderId()));
    }

    public RequestChatResDto requestChat(Long receiverId, Long senderId) {
        String requestKey = "chat:chatRequest:" + senderId + ":" + receiverId;

        if (receiverId.equals(senderId)) throw new SameParticipantException("자기 자신과는 채팅할 수 없습니다.");
        if (chatRequestRepository.existsById(requestKey)) throw new AlreadyRequestException("이미 요청된 채팅입니다.");

        chatRepositoryFacade.findChatRoomByGuestsId(receiverId, senderId)
                            .map(chatRoom -> getChatParticipant(chatRoom.getId(), senderId))
                            .ifPresent(participant -> {
                                if (participant.isActiveParticipant()) {
                                    throw new AlreadyActiveChatException("이미 참가 중인 채팅방이 존재합니다.");
                                }
                            });

        LocalDateTime now = LocalDateTime.now();
        Duration requestTTL = Duration.ofDays(1);

        Guest sender = guestRepository.findById(senderId)
                                      .orElseThrow(() -> new EntityNotFoundException("요청자 정보(id: %d)를 찾을 수 없습니다.".formatted(senderId)));
        Guest receiver = guestRepository.findById(receiverId)
                                        .orElseThrow(() -> new EntityNotFoundException("수신자 정보(id: %d)를 찾을 수 없습니다.".formatted(receiverId)));

        ChatRequest chatRequest = ChatRequest.builder()
                                             .requestId(requestKey)
                                             .senderId(senderId)
                                             .senderName(sender.getName())
                                             .senderProfileImage(sender.getProfileUrl())
                                             .receiverId(receiverId)
                                             .receiverName(receiver.getName())
                                             .receiverProfileImage(receiver.getProfileUrl())
                                             .createdAt(now)
                                             .expiresAt(now.plus(requestTTL))
                                             .build();
        chatRequestRepository.save(chatRequest);

        eventPublisher.publishEvent(new ChatRequestCreatedEvent(chatRequest));

        return chatRequest.toResDto();
    }

    public List<ChatRoomResDto> getChatRooms(Long guestId) {
        return chatRepositoryFacade.findChatRoomsByGuestId(guestId);
    }

    public boolean isChatRoomParticipant(Long roomId, Long guestId) {
        return chatRepositoryFacade.findByChatRoomIdAndGuestId(roomId, guestId)
                                   .map(ChatParticipant::isActiveParticipant)
                                   .orElse(false);
    }

    public List<RequestChatResDto> getReceivedChatRequests(Long guestId) {
        return chatRequestRepository.findByReceiverId(guestId)
                                    .stream()
                                    .map(ChatRequest::toResDto)
                                    .toList();
    }

    public List<RequestChatResDto> getSentChatRequests(Long guestId) {
        return chatRequestRepository.findBySenderId(guestId)
                                    .stream()
                                    .map(ChatRequest::toResDto)
                                    .toList();
    }

    private ChatRoom createNewChatRoom(Long receiverId, Long senderId) {
        Guest receiver = getGuestById(receiverId);
        Guest sender = getGuestById(senderId);

        ChatRoom chatRoom = chatRepositoryFacade.saveChatRoom(new ChatRoom());

        List<ChatParticipant> newParticipants = List.of(
                ChatParticipant.builder()
                               .chatRoom(chatRoom)
                               .guest(receiver)
                               .isCreator(false)
                               .customRoomName(sender.getName() + "님과의 대화")
                               .build(),
                ChatParticipant.builder()
                               .chatRoom(chatRoom)
                               .guest(sender)
                               .isCreator(true)
                               .customRoomName(receiver.getName() + "님과의 대화")
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
