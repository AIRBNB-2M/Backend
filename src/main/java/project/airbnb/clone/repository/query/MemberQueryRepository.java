package project.airbnb.clone.repository.query;

import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import project.airbnb.clone.dto.member.ChatMemberSearchDto;
import project.airbnb.clone.dto.member.TripHistoryResDto;
import project.airbnb.clone.entity.member.Member;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;
import project.airbnb.clone.repository.query.support.CustomQuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static project.airbnb.clone.entity.accommodation.QAccommodation.accommodation;
import static project.airbnb.clone.entity.accommodation.QAccommodationImage.accommodationImage;
import static project.airbnb.clone.entity.member.QMember.member;
import static project.airbnb.clone.entity.reservation.QReservation.reservation;
import static project.airbnb.clone.entity.reservation.QReview.review;

@Repository
public class MemberQueryRepository extends CustomQuerydslRepositorySupport {

    public MemberQueryRepository() {
        super(Member.class);
    }

    public Optional<DefaultProfileQueryDto> getDefaultProfile(Long memberId) {
        return Optional.ofNullable(
                select(Projections.constructor(
                        DefaultProfileQueryDto.class,
                        member.name,
                        member.profileUrl,
                        member.createdAt,
                        member.aboutMe,
                        member.isEmailVerified))
                        .from(member)
                        .where(member.id.eq(memberId))
                        .fetchOne()
        );
    }

    public List<ChatMemberSearchDto> findMembersByName(String name) {
        return select(Projections.constructor(
                ChatMemberSearchDto.class,
                member.id,
                member.name,
                member.createdAt,
                member.profileUrl))
                .from(member)
                .where(member.name.contains(name))
                .fetch();
    }

    public Page<TripHistoryResDto> getTripsHistory(Long memberId, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery ->
                        contentQuery.select(Projections.constructor(
                                            TripHistoryResDto.class,
                                            reservation.id,
                                            accommodation.id,
                                            accommodationImage.imageUrl,
                                            accommodation.title,
                                            reservation.startDate,
                                            reservation.endDate,
                                            review.isNotNull()))
                                    .from(accommodation)
                                    .join(accommodationImage)
                                    .on(accommodationImage.accommodation.eq(accommodation)
                                                                        .and(accommodationImage.thumbnail.isTrue()))
                                    .leftJoin(reservation)
                                    .on(reservation.accommodation.eq(accommodation))
                                    .leftJoin(review).on(review.reservation.eq(reservation))
                                    .where(
                                            reservation.isNotNull(),
                                            reservation.member.id.eq(memberId),
                                            reservation.endDate.before(LocalDateTime.now()))
                                    .orderBy(reservation.id.desc())
                ,
                countQuery -> countQuery.select(reservation.count())
                                        .from(reservation)
                                        .where(
                                                reservation.member.id.eq(memberId),
                                                reservation.endDate.before(LocalDateTime.now())
                                        )
        );
    }
}
