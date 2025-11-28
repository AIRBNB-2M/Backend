package project.airbnb.clone.common.exceptions.factory;

import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;

public abstract class ChatExceptions {

    public static BusinessException participantLeft(Long chatRoomId, Long memberId) {
        return new BusinessException(
                ErrorCode.PARTICIPANT_LEFT,
                String.format("chatRoomId=%d 채팅방의 memberId=%d 사용자가 채팅방을 나가 메시지 전송에 실패", chatRoomId, memberId)
        );
    }

    public static BusinessException sameParticipant(Long memberId) {
        return new BusinessException(
                ErrorCode.SAME_PARTICIPANT,
                String.format("자신에게 채팅을 요청했습니다. memberId=%d", memberId)
        );
    }

    public static BusinessException alreadyRequest(String requestId) {
        return new BusinessException(
                ErrorCode.ALREADY_REQUEST_SENT,
                String.format("이미 채팅 요청된 기록이 존재. requestId=%s", requestId)
        );
    }

    public static BusinessException alreadyActiveChat() {
        return new BusinessException(ErrorCode.ALREADY_ACTIVE_CHAT);
    }

    public static BusinessException notFoundChatParticipant(Long chatRoomId, Long memberId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("chatRoomId=%d 채팅방에서 memberId=%d 사용자 조회 실패", chatRoomId, memberId)
        );
    }

    public static BusinessException notFoundChatRequest(String requestId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("요청이 존재하지 않거나 만료. requestId=%s" , requestId)
        );
    }

    public static BusinessException notFoundChatRoom(Long roomId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("roomId=%d 채팅방 조회 실패" , roomId)
        );
    }

    public static BusinessException notFoundChatRoom(Long currentMemberId, Long otherMemberId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("id=%d 사용자와 id=%d 사용자의 채팅방 조회 실패", currentMemberId, otherMemberId)
        );
    }

    public static BusinessException notFoundChatMessage(Long messageId) {
        return new BusinessException(
                ErrorCode.ENTITY_NOT_FOUND,
                String.format("messageId=%d 채팅 메시지 조회 실패" , messageId)
        );
    }

    public static BusinessException notOwnerOfChatRequest(String requestId, Long receiverId) {
        return new BusinessException(
                ErrorCode.ACCESS_DENIED,
                String.format("requestId=%s 채팅 요청의 수신자가 아닙니다. receiverId=%d", requestId, receiverId)
        );
    }
}
