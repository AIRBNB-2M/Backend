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

import static project.airbnb.clone.entity.chat.QChatParticipant.chatParticipant;

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

    public boolean existsByTwoGuestIds(Long guestId1, Long guestId2) {
        QChatParticipant cp1 = chatParticipant;
        QChatParticipant cp2 = new QChatParticipant("cp2");

        return queryFactory.selectOne()
                           .from(cp1)
                           .join(cp2).on(cp1.chatRoom.eq(cp2.chatRoom))
                           .where(cp1.guest.id.eq(guestId1).and(cp2.guest.id.eq(guestId2)))
                           .fetchFirst() != null;
    }

    public Optional<ChatRoomResDto> findChatRoomInfo(Long otherGuestId, Long creatorId, ChatRoom chatRoom) {
        BooleanExpression otherGuestCond = CP2.chatRoom.eq(CR).and(CP2.guest.id.eq(otherGuestId));

        return Optional.ofNullable(buildChatRoomQuery(creatorId, otherGuestCond)
                .where(CR.eq(chatRoom))
                .fetchOne()
        );
    }

    public List<ChatRoomResDto> findChatRooms(Long guestId) {
        BooleanExpression otherGuestCond = CP2.chatRoom.eq(CR).and(CP2.guest.id.ne(guestId));

        return buildChatRoomQuery(guestId, otherGuestCond).fetch();
    }

    private JPQLQuery<ChatRoomResDto> buildChatRoomQuery(Long guestId, BooleanExpression otherGuestCond) {
        JPQLQuery<Integer> unreadCount = JPAExpressions.select(SUB_CM.count().intValue())
                                                        .from(SUB_CM)
                                                        .where(SUB_CM.chatRoom.eq(CR).and(CP1.lastReadMessage.isNull()
                                                                                                             .or(SUB_CM.id.gt(CP1.lastReadMessage.id))));
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
                        unreadCount
                ))
                .from(CR)
                .join(CP1).on(CP1.chatRoom.eq(CR),
                        CP1.guest.id.eq(guestId),
                        CP1.isActive.isTrue()
                )
                .join(CP2).on(otherGuestCond)
                .join(CP2.guest, OTHER_GUEST)
                .leftJoin(CM).on(CM.id.eq(
                        JPAExpressions.select(SUB_CM.id.max())
                                      .from(SUB_CM)
                                      .where(SUB_CM.chatRoom.eq(CR))
                ));
    }
}
