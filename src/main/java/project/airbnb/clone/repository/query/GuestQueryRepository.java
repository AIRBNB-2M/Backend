package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;

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
                                guest.aboutMe
                        ))
                        .from(guest)
                        .where(guest.id.eq(guestId))
                        .fetchOne()
        );
    }
}
