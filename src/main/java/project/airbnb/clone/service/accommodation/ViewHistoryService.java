package project.airbnb.clone.service.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.entity.ViewHistory;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.jpa.ViewHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewHistoryService {

    private final MemberRepository memberRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final AccommodationRepository accommodationRepository;

    public void saveRecentView(Long accommodationId, Long memberId) {
        int updated = viewHistoryRepository.updateViewedAt(accommodationId, memberId, LocalDateTime.now());

        if (updated == 0) {
            Accommodation accommodation = accommodationRepository.getReferenceById(accommodationId);
            Member member = memberRepository.getReferenceById(memberId);

            viewHistoryRepository.save(ViewHistory.ofNow(accommodation, member));
        }
    }
}
