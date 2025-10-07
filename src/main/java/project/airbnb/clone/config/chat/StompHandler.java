package project.airbnb.clone.config.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.service.chat.ChatService;

import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.SEND;
import static org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE;
import static project.airbnb.clone.common.jwt.JwtProperties.AUTHORIZATION_HEADER;
import static project.airbnb.clone.common.jwt.JwtProperties.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (CONNECT == accessor.getCommand()) {
            String token = getToken(accessor);
            jwtProvider.validateToken(token);
        }
        else if (SUBSCRIBE == accessor.getCommand() || SEND == accessor.getCommand()) {
            String token = getToken(accessor);
            jwtProvider.validateToken(token);

            validateToken(accessor);

            Long roomId = extractRoomId(accessor.getDestination());
            if (roomId == null) {
                log.warn("{} 요청: destination에서 roomId 추출 실패", accessor.getCommand());
                return null;
            }
            Long guestId = jwtProvider.getId(token);

            if (!chatService.isChatRoomParticipant(roomId, guestId)) {
                return null;
            }
        }

        return message;
    }

    private Long extractRoomId(String destination) {
        if (!StringUtils.hasText(destination)) {
            return null;
        }

        String[] parts = destination.split("/");
        if (parts.length < 3) {
            return null;
        }

        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            log.warn("roomId 파싱 실패: {}", parts[2], e);
            return null;
        }
    }

    private void validateToken(StompHeaderAccessor accessor) {
        String token = getToken(accessor);
        jwtProvider.validateToken(token);
    }

    private String getToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        return bearerToken.substring(TOKEN_PREFIX.length());
    }
}
