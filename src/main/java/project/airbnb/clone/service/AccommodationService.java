package project.airbnb.clone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.accommodation.AreaListResDto;
import project.airbnb.clone.dto.accommodation.MainAccListResDto;
import project.airbnb.clone.repository.query.AccommodationQueryRepository;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationService {

    private final AccommodationQueryRepository accommodationQueryRepository;

    public List<AreaListResDto<MainAccListResDto>> getAccommodations(Long guestId) {
        //TODO : 데이터 많아지면 네이티브 쿼리 고려
        List<MainAccListResDto> accommodations = accommodationQueryRepository.getAreaAccommodations(Season.today(), DayType.today(), guestId);

        return accommodations
                .stream()
                .collect(groupingBy(
                        MainAccListResDto::getAreaCode,
                        collectingAndThen(
                                toList(),
                                dtos -> dtos.stream().limit(8).toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> new AreaListResDto<>(entry.getKey(), entry.getValue()))
                .toList();
    }

}
