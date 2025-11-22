package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.entity.QGuest;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.entity.chat.QChatMessage;
import project.airbnb.clone.entity.chat.QChatParticipant;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

import static project.airbnb.clone.entity.chat.QChatMessage.chatMessage;
import static project.airbnb.clone.entity.chat.QChatRoom.chatRoom;

@Repository
public class ChatRoomQueryRepository extends CustomQuerydslRepositorySupport {

    private static final QChatParticipant CP1 = new QChatParticipant("cp1");
    private static final QChatParticipant CP2 = new QChatParticipant("cp2");
    private static final QGuest OTHER_GUEST = new QGuest("otherGuest");
    private static final QChatMessage SUB_CM = new QChatMessage("subMessage");

    public ChatRoomQueryRepository() {
        super(ChatRoom.class);
    }

    public Optional<ChatRoomResDto> findChatRoomInfo(Long currentGuestId, Long otherGuestId, ChatRoom targetRoom) {
        BooleanExpression currentUserCond = CP1.chatRoom.eq(chatRoom).and(CP1.guest.id.eq(currentGuestId));
        BooleanExpression otherUserCond = CP2.chatRoom.eq(chatRoom).and(CP2.guest.id.eq(otherGuestId));

        return Optional.ofNullable(
                select(Projections.constructor(
                        ChatRoomResDto.class,
                        chatRoom.id,
                        CP1.customRoomName,
                        OTHER_GUEST.id,
                        OTHER_GUEST.name,
                        OTHER_GUEST.profileUrl,
                        CP2.isActive,
                        chatMessage.content,
                        chatMessage.createdAt,
                        buildUnreadCountSubQuery()))
                        .from(chatRoom)
                        .join(CP1).on(currentUserCond)
                        .join(CP2).on(otherUserCond)
                        .join(CP2.guest, OTHER_GUEST)
                        .leftJoin(chatMessage).on(chatMessage.id.eq(
                                JPAExpressions.select(SUB_CM.id.max())
                                              .from(SUB_CM)
                                              .where(SUB_CM.chatRoom.eq(chatRoom))
                        ))
                        .where(chatRoom.eq(targetRoom))
                        .fetchOne()
        );
    }

    public List<ChatRoomResDto> findChatRooms(Long guestId) {
        // CP1: 현재 사용자의 참가 정보
        // CP2: 상대방의 참가 정보
        BooleanExpression currentUserCond = CP1.chatRoom.eq(chatRoom)
                                                        .and(CP1.guest.id.eq(guestId))
                                                        .and(CP1.isActive.isTrue());
        BooleanExpression otherUserCond = CP2.chatRoom.eq(chatRoom)
                                                      .and(CP2.guest.id.ne(guestId));

        return select(Projections.constructor(
                ChatRoomResDto.class,
                chatRoom.id,
                CP1.customRoomName,
                OTHER_GUEST.id,
                OTHER_GUEST.name,
                OTHER_GUEST.profileUrl,
                CP2.isActive,
                chatMessage.content,
                chatMessage.createdAt,
                buildUnreadCountSubQuery()))
                .from(chatRoom)
                .join(CP1).on(currentUserCond)
                .join(CP2).on(otherUserCond)
                .join(CP2.guest, OTHER_GUEST)
                .leftJoin(chatMessage).on(chatMessage.id.eq(
                        JPAExpressions.select(SUB_CM.id.max())
                                      .from(SUB_CM)
                                      .where(SUB_CM.chatRoom.eq(chatRoom))
                ))
                .orderBy(chatMessage.createdAt.desc().nullsLast())
                .fetch();
    }

    private JPQLQuery<Integer> buildUnreadCountSubQuery() {
        return JPAExpressions
                .select(SUB_CM.count().intValue())
                .from(SUB_CM)
                .where(SUB_CM.chatRoom.eq(chatRoom)
                                      .and(CP1.lastReadMessage.isNull().or(SUB_CM.id.gt(CP1.lastReadMessage.id)))
                                      // 본인이 보낸 메시지 제외
                                      .and(SUB_CM.writer.ne(CP1.guest))
                );
    }
}
