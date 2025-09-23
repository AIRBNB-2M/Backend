package project.airbnb.clone.service.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.ViewHistory;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.jpa.ViewHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewHistoryService {

    private final GuestRepository guestRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final AccommodationRepository accommodationRepository;

    public void saveRecentView(Long accommodationId, Long guestId) {
        int updated = viewHistoryRepository.updateViewedAt(accommodationId, guestId);

        if (updated == 0) {
            Accommodation accommodation = accommodationRepository.getReferenceById(accommodationId);
            Guest guest = guestRepository.getReferenceById(guestId);

            viewHistoryRepository.save(ViewHistory.builder()
                                                  .accommodation(accommodation)
                                                  .guest(guest)
                                                  .viewedAt(LocalDateTime.now())
                                                  .build());
        }
    }
}
