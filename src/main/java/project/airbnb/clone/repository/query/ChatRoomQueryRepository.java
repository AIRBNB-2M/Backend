package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.chat.ChatRoomResDto;
import project.airbnb.clone.entity.QGuest;
import project.airbnb.clone.entity.chat.ChatRoom;
import project.airbnb.clone.entity.chat.QChatMessage;
import project.airbnb.clone.entity.chat.QChatParticipant;
import project.airbnb.clone.entity.chat.QChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryRepository {

    private static final QChatRoom CR = QChatRoom.chatRoom;
    private static final QChatParticipant CP1 = new QChatParticipant("cp1");
    private static final QChatParticipant CP2 = new QChatParticipant("cp2");
    private static final QGuest OTHER_GUEST = new QGuest("otherGuest");
    private static final QChatMessage CM = QChatMessage.chatMessage;
    private static final QChatMessage SUB_CM = new QChatMessage("subMessage");

    private final JPAQueryFactory queryFactory;

    public Optional<ChatRoomResDto> findChatRoomInfo(Long currentGuestId, Long otherGuestId, ChatRoom chatRoom) {
        BooleanExpression currentUserCond = CP1.chatRoom.eq(CR).and(CP1.guest.id.eq(currentGuestId));
        BooleanExpression otherUserCond = CP2.chatRoom.eq(CR).and(CP2.guest.id.eq(otherGuestId));

        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(ChatRoomResDto.class,
                                CR.id,
                                CP1.customRoomName,
                                OTHER_GUEST.id,
                                OTHER_GUEST.name,
                                OTHER_GUEST.profileUrl,
                                CP2.isActive,
                                CM.content,
                                CM.createdAt,
                                buildUnreadCountSubQuery()
                        ))
                        .from(CR)
                        .join(CP1).on(currentUserCond)
                        .join(CP2).on(otherUserCond)
                        .join(CP2.guest, OTHER_GUEST)
                        .leftJoin(CM).on(CM.id.eq(
                                JPAExpressions.select(SUB_CM.id.max())
                                              .from(SUB_CM)
                                              .where(SUB_CM.chatRoom.eq(CR))
                        ))
                        .where(CR.eq(chatRoom))
                        .fetchOne()
        );
    }

    public List<ChatRoomResDto> findChatRooms(Long guestId) {
        // CP1: 현재 사용자의 참가 정보
        // CP2: 상대방의 참가 정보
        BooleanExpression currentUserCond = CP1.chatRoom.eq(CR)
                                                        .and(CP1.guest.id.eq(guestId))
                                                        .and(CP1.isActive.isTrue());
        BooleanExpression otherUserCond = CP2.chatRoom.eq(CR)
                                                      .and(CP2.guest.id.ne(guestId));

        return queryFactory
                .select(Projections.constructor(ChatRoomResDto.class,
                        CR.id,
                        CP1.customRoomName,
                        OTHER_GUEST.id,
                        OTHER_GUEST.name,
                        OTHER_GUEST.profileUrl,
                        CP2.isActive,
                        CM.content,
                        CM.createdAt,
                        buildUnreadCountSubQuery()
                ))
                .from(CR)
                .join(CP1).on(currentUserCond)
                .join(CP2).on(otherUserCond)
                .join(CP2.guest, OTHER_GUEST)
                .leftJoin(CM).on(CM.id.eq(
                        JPAExpressions.select(SUB_CM.id.max())
                                      .from(SUB_CM)
                                      .where(SUB_CM.chatRoom.eq(CR))
                ))
                .orderBy(CM.createdAt.desc().nullsLast())
                .fetch();
    }

    private JPQLQuery<Integer> buildUnreadCountSubQuery() {
        return JPAExpressions
                .select(SUB_CM.count().intValue())
                .from(SUB_CM)
                .where(SUB_CM.chatRoom.eq(CR)
                                      .and(CP1.lastReadMessage.isNull().or(SUB_CM.id.gt(CP1.lastReadMessage.id)))
                                      // 본인이 보낸 메시지 제외
                                      .and(SUB_CM.writer.ne(CP1.guest))
                );
    }
}
