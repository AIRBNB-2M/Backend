package project.airbnb.clone.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ChatRequestRepository chatRequestRepository;
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

    @Transactional
    public ChatRoomResDto acceptRequestChat(String requestId, Long accepterId) {
        ChatRequest chatRequest = chatRequestRepository.findById(requestId)
                                                       .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않거나 만료되었습니다. => " + requestId));

        if (!chatRequest.getReceiverId().equals(accepterId)) throw new AccessDeniedException("본인에게 온 요청만 수락할 수 있습니다.");

        chatRequestRepository.delete(chatRequest);

        ChatRoom chatRoom = chatRepositoryFacade.findChatRoomByGuestsId(chatRequest.getSenderId(), accepterId)
                                                .map(existingRoom -> {
                                                    reactiveIfHasLeft(existingRoom.getId(), chatRequest.getSenderId());
                                                    reactiveIfHasLeft(existingRoom.getId(), accepterId);
                                                    return existingRoom;
                                                })
                                                .orElseGet(() -> createNewChatRoom(chatRequest.getSenderId(), accepterId));

        return chatRepositoryFacade.getChatRoomInfo(accepterId, chatRequest.getSenderId(), chatRoom);
    }

    public void rejectRequestChat(String requestId, Long rejecterId) {
        ChatRequest chatRequest = chatRequestRepository.findById(requestId)
                                                       .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않거나 만료되었습니다. => " + requestId));

        if (!chatRequest.getReceiverId().equals(rejecterId)) throw new AccessDeniedException("본인에게 온 요청만 거절할 수 있습니다.");

        chatRequestRepository.delete(chatRequest);
        // TODO: WebSocket 이벤트 발행 (요청자에게 거절 알림)
        // eventPublisher.publishEvent(new ChatRequestRejectedEvent(request));
    }

    public RequestChatResDto requestChat(Long receiverId, Long senderId) {
        String requestKey = "chat:request:" + senderId + ":" + receiverId;

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

        ChatRequest request = ChatRequest.builder()
                                         .senderId(senderId)
                                         .senderName(sender.getName())
                                         .senderProfileImage(sender.getProfileUrl())
                                         .receiverId(receiverId)
                                         .receiverName(receiver.getName())
                                         .receiverProfileImage(receiver.getProfileUrl())
                                         .createdAt(now)
                                         .expiresAt(now.plus(requestTTL))
                                         .build();
        chatRequestRepository.save(request);

        // TODO : WebSocket 이벤트 발행
        // eventPublisher.publishEvent(new ChatRequestCreatedEvent(senderId, receiverId, request.getExpiresAt()));

        return request.toResDto();
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
