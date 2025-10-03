package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.entity.QGuest;
import project.airbnb.clone.entity.chat.QChatMessage;
import project.airbnb.clone.entity.chat.QChatRoom;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageQueryRepository {

    private static final QChatRoom CR = QChatRoom.chatRoom;
    private static final QChatMessage CM = QChatMessage.chatMessage;
    private static final QGuest GUEST = QGuest.guest;

    private final JPAQueryFactory queryFactory;

    public List<ChatMessageResDto> getMessages(Long lastMessageId, Long roomId, int pageSize) {
        return queryFactory.select(Projections.constructor(ChatMessageResDto.class,
                                   CM.id,
                                   CR.id,
                                   GUEST.id,
                                   GUEST.name,
                                   CM.content,
                                   CM.createdAt
                           ))
                           .from(CM)
                           .join(CM.chatRoom, CR)
                           .join(CM.writer, GUEST)
                           .where(CR.id.eq(roomId).and(lastMessageId != null ? CM.id.lt(lastMessageId) : null))
                           .orderBy(CM.id.desc())
                           .limit(pageSize + 1)
                           .fetch();
    }
}
