package project.airbnb.clone.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.events.chat.ChatLeaveEvent;
import project.airbnb.clone.common.events.chat.ChatRequestAcceptedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestCreatedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestRejectedEvent;
import project.airbnb.clone.common.exceptions.chat.AlreadyActiveChatException;
import project.airbnb.clone.common.exceptions.chat.AlreadyRequestException;
import project.airbnb.clone.common.exceptions.chat.ParticipantLeftException;
import project.airbnb.clone.common.exceptions.chat.SameParticipantException;
import project.airbnb.clone.dto.chat.*;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.entity.chat.ChatMessage;
import project.airbnb.clone.entity.chat.ChatParticipant;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.repository.dto.ChatRequest;
import project.airbnb.clone.repository.facade.ChatRepositoryFacadeManager;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.redis.ChatRequestRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatRepositoryFacadeManager chatRepositoryFacade;

    @Transactional
    public ChatMessageResDto saveChatMessage(Long roomId, ChatMessageReqDto chatMessageDto) {
        ChatRoom chatRoom = chatRepositoryFacade.getChatRoomByRoomId(roomId);
        List<ChatParticipant> participants = chatRepositoryFacade.findParticipantsByChatRoom(chatRoom);

        for (ChatParticipant participant : participants) {
            if (participant.hasLeft()) {
                throw new ParticipantLeftException("Cannot send message: one of the participants has left the chat, left memberId: " + participant.getMember().getId());
            }
        }

        Long senderId = chatMessageDto.senderId();
        String content = chatMessageDto.content();

        Member writer = geteMemberyId(senderId);
        ChatMessage message = chatRepositoryFacade.saveChatMessage(ChatMessage.create(chatRoom, writer, content));

        ChatParticipant chatParticipant = participants.stream()
                                                      .filter(participant -> participant.getMember().getId().equals(writer.getId()))
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
                                .isLeft(false)
                                .build();
    }

    @Transactional
    public ChatMessagesResDto getMessageHistories(Long lastMessageId, Long roomId, int pageSize, Long memberId) {
        List<ChatMessageResDto> messages = chatRepositoryFacade.getMessages(lastMessageId, roomId, pageSize);

        if (lastMessageId == null && !messages.isEmpty()) {
            Long lastId = messages.get(0).messageId();
            ChatParticipant chatParticipant = getChatParticipant(roomId, memberId);
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
    public ChatRoomResDto updateChatRoomName(String customName, Long othermemberId, Long myId, Long roomId) {
        ChatRoom chatRoom = chatRepositoryFacade.getChatRoomByRoomId(roomId);

        int updated = chatRepositoryFacade.updateCustomName(customName, chatRoom, myId);
        if (updated == 0) {
            throw new EntityNotFoundException("ChatParticipant with roomId: " + roomId + " and memberId: " + myId + "cannot be found");
        }

        return chatRepositoryFacade.getChatRoomInfo(othermemberId, myId, chatRoom);
    }

    @Transactional
    public void leaveChatRoom(Long roomId, Long memberId, Boolean active) {
        ChatParticipant chatParticipant = getChatParticipant(roomId, memberId);
        chatParticipant.leave();

        chatRepositoryFacade.markLatestMessageAsRead(roomId, chatParticipant);
        eventPublisher.publishEvent(new ChatLeaveEvent(chatParticipant.getMember().getName(), roomId));
    }

    @Transactional
    public ChatRoomResDto acceptRequestChat(String requestId, Long receiverId) {
        ChatRequest chatRequest = chatRequestRepository.findById(requestId)
                                                       .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않거나 만료되었습니다. => " + requestId));

        if (!chatRequest.getReceiverId().equals(receiverId)) throw new AccessDeniedException("본인에게 온 요청만 수락할 수 있습니다.");

        chatRequestRepository.delete(chatRequest);

        Long senderId = chatRequest.getSenderId();
        ChatRoom chatRoom = chatRepositoryFacade.findChatRoomByMembersId(receiverId, senderId)
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

        chatRepositoryFacade.findChatRoomByMembersId(receiverId, senderId)
                            .map(chatRoom -> getChatParticipant(chatRoom.getId(), senderId))
                            .ifPresent(participant -> {
                                if (participant.isActiveParticipant()) {
                                    throw new AlreadyActiveChatException("이미 참가 중인 채팅방이 존재합니다.");
                                }
                            });

        LocalDateTime now = LocalDateTime.now();
        Duration requestTTL = Duration.ofDays(1);

        Member sender = memberRepository.findById(senderId)
                                        .orElseThrow(() -> new EntityNotFoundException("요청자 정보(id: %d)를 찾을 수 없습니다.".formatted(senderId)));
        Member receiver = memberRepository.findById(receiverId)
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

    public List<ChatRoomResDto> getChatRooms(Long memberId) {
        return chatRepositoryFacade.findChatRoomsByMemberId(memberId);
    }

    public boolean isChatRoomParticipant(Long roomId, Long memberId) {
        return chatRepositoryFacade.findByChatRoomIdAndMemberId(roomId, memberId)
                                   .map(ChatParticipant::isActiveParticipant)
                                   .orElse(false);
    }

    public List<RequestChatResDto> getReceivedChatRequests(Long memberId) {
        return chatRequestRepository.findByReceiverId(memberId)
                                    .stream()
                                    .map(ChatRequest::toResDto)
                                    .toList();
    }

    public List<RequestChatResDto> getSentChatRequests(Long memberId) {
        return chatRequestRepository.findBySenderId(memberId)
                                    .stream()
                                    .map(ChatRequest::toResDto)
                                    .toList();
    }

    @Transactional
    public void markChatRoomAsRead(Long roomId, Long memberId) {
        ChatParticipant chatParticipant = getChatParticipant(roomId, memberId);
        chatRepositoryFacade.markLatestMessageAsRead(roomId, chatParticipant);
    }

    private ChatRoom createNewChatRoom(Long receiverId, Long senderId) {
        Member receiver = geteMemberyId(receiverId);
        Member sender = geteMemberyId(senderId);

        ChatRoom chatRoom = chatRepositoryFacade.saveChatRoom(new ChatRoom());

        List<ChatParticipant> newParticipants = List.of(
                ChatParticipant.participant(chatRoom, receiver, sender.getName() + "님과의 대화"),
                ChatParticipant.creator(chatRoom, sender, receiver.getName() + "님과의 대화")
        );

        chatRepositoryFacade.saveChatParticipants(newParticipants);
        return chatRoom;
    }

    private void reactiveIfHasLeft(Long roomId, Long memberId) {
        ChatParticipant chatParticipant = getChatParticipant(roomId, memberId);
        if (chatParticipant.hasLeft()) {
            chatParticipant.rejoin();
        }
    }

    private Member geteMemberyId(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> new EntityNotFoundException("Guest with id " + memberId + "cannot be found"));
    }

    private ChatParticipant getChatParticipant(Long roomId, Long memberId) {
        return chatRepositoryFacade.findByChatRoomIdAndMemberId(roomId, memberId)
                                   .orElseThrow(() -> new EntityNotFoundException("ChatParticipant with roomId: " + roomId + " and memberId: " + memberId + "cannot be found"));
    }
}
