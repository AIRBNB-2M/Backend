package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.chat.ChatMessageResDto;
import project.airbnb.clone.entity.chat.ChatMessage;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.util.List;

import static project.airbnb.clone.entity.chat.QChatMessage.chatMessage;
import static project.airbnb.clone.entity.chat.QChatParticipant.chatParticipant;
import static project.airbnb.clone.entity.chat.QChatRoom.chatRoom;
import static project.airbnb.clone.entity.member.QMember.member;

@Repository
public class ChatMessageQueryRepository extends CustomQuerydslRepositorySupport {

    public ChatMessageQueryRepository() {
        super(ChatMessage.class);
    }

    public List<ChatMessageResDto> getMessages(Long lastMessageId, Long roomId, int pageSize) {

        return select(Projections.constructor(
                ChatMessageResDto.class,
                chatMessage.id,
                chatRoom.id,
                member.id,
                member.name,
                chatMessage.content,
                chatMessage.createdAt))
                .from(chatMessage)
                .join(chatMessage.chatRoom, chatRoom)
                .join(chatMessage.writer, member)
                .join(chatParticipant).on(chatParticipant.chatRoom.eq(chatRoom).and(chatParticipant.member.eq(member)))
                .where(chatRoom.id.eq(roomId),
                        lastMessageId != null ? chatMessage.id.lt(lastMessageId) : null,
                        chatParticipant.lastRejoinedAt.isNull()
                                                      .or(chatMessage.createdAt.after(chatParticipant.lastRejoinedAt))
                )
                .orderBy(chatMessage.id.desc())
                .limit(pageSize + 1)
                .fetch();
    }

}
