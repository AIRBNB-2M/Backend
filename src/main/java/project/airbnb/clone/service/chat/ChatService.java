package project.airbnb.clone.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.events.chat.ChatLeaveEvent;
import project.airbnb.clone.common.events.chat.ChatRequestAcceptedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestCreatedEvent;
import project.airbnb.clone.common.events.chat.ChatRequestRejectedEvent;
import project.airbnb.clone.common.exceptions.factory.ChatExceptions;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
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
                throw ChatExceptions.participantLeft(chatRoom.getId(), participant.getMember().getId());
            }
        }

        Long senderId = chatMessageDto.senderId();
        String content = chatMessageDto.content();

        Member writer = getMemberById(senderId);
        ChatMessage message = chatRepositoryFacade.saveChatMessage(ChatMessage.create(chatRoom, writer, content));

        ChatParticipant chatParticipant = participants.stream()
                                                      .filter(participant -> participant.getMember().getId().equals(writer.getId()))
                                                      .findFirst()
                                                      .orElseThrow(() -> ChatExceptions.notFoundChatParticipant(chatRoom.getId(), writer.getId()));
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
    public ChatRoomResDto updateChatRoomName(String customName, Long otherMemberId, Long myId, Long roomId) {
        ChatRoom chatRoom = chatRepositoryFacade.getChatRoomByRoomId(roomId);

        int updated = chatRepositoryFacade.updateCustomName(customName, chatRoom, myId);
        if (updated == 0) {
            throw ChatExceptions.notFoundChatParticipant(roomId, myId);
        }

        return chatRepositoryFacade.getChatRoomInfo(myId, otherMemberId, chatRoom);
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
                                                       .orElseThrow(() -> ChatExceptions.notFoundChatRequest(requestId));

        if (!chatRequest.getReceiverId().equals(receiverId)) {
            throw ChatExceptions.notOwnerOfChatRequest(requestId, receiverId);
        }

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
                                                       .orElseThrow(() -> ChatExceptions.notFoundChatRequest(requestId));

        if (!chatRequest.getReceiverId().equals(rejecterId)) {
            throw ChatExceptions.notOwnerOfChatRequest(requestId, rejecterId);
        }

        chatRequestRepository.delete(chatRequest);
        eventPublisher.publishEvent(new ChatRequestRejectedEvent(requestId, chatRequest.getSenderId()));
    }

    public RequestChatResDto requestChat(Long receiverId, Long senderId) {
        String requestKey = "chat:chatRequest:" + senderId + ":" + receiverId;

        if (receiverId.equals(senderId)) throw ChatExceptions.sameParticipant(receiverId);
        if (chatRequestRepository.existsById(requestKey)) throw ChatExceptions.alreadyRequest(requestKey);

        chatRepositoryFacade.findChatRoomByMembersId(receiverId, senderId)
                            .map(chatRoom -> getChatParticipant(chatRoom.getId(), senderId))
                            .ifPresent(participant -> {
                                if (participant.isActiveParticipant()) {
                                    throw ChatExceptions.alreadyActiveChat();
                                }
                            });

        LocalDateTime now = LocalDateTime.now();
        Duration requestTTL = Duration.ofDays(1);

        Member sender = memberRepository.findById(senderId).orElseThrow(() -> MemberExceptions.notFoundById(senderId));
        Member receiver = memberRepository.findById(receiverId).orElseThrow(() -> MemberExceptions.notFoundById(receiverId));

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
        Member receiver = getMemberById(receiverId);
        Member sender = getMemberById(senderId);

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

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> MemberExceptions.notFoundById(memberId));
    }

    private ChatParticipant getChatParticipant(Long roomId, Long memberId) {
        return chatRepositoryFacade.findByChatRoomIdAndMemberId(roomId, memberId)
                                   .orElseThrow(() -> ChatExceptions.notFoundChatParticipant(roomId, memberId));
    }
}
