package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.guest.ChatGuestSearchDto;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;

import java.util.List;
import java.util.Optional;

import static project.airbnb.clone.entity.QGuest.guest;

@Repository
@RequiredArgsConstructor
public class GuestQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<DefaultProfileQueryDto> getDefaultProfile(Long guestId) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(DefaultProfileQueryDto.class,
                                guest.name,
                                guest.profileUrl,
                                guest.createdAt,
                                guest.aboutMe,
                                guest.isEmailVerified
                        ))
                        .from(guest)
                        .where(guest.id.eq(guestId))
                        .fetchOne()
        );
    }

    public List<ChatGuestSearchDto> findGuestsByName(String name) {
        return queryFactory
                .select(Projections.constructor(ChatGuestSearchDto.class,
                        guest.id,
                        guest.name,
                        guest.createdAt,
                        guest.profileUrl
                ))
                .from(guest)
                .where(guest.name.contains(name))
                .fetch();
    }
}
