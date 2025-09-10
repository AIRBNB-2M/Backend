package project.airbnb.clone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.AccSearchCondDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccResDto;
import project.airbnb.clone.dto.accommodation.MainAccListQueryDto;
import project.airbnb.clone.dto.accommodation.MainAccListResDto;
import project.airbnb.clone.repository.query.AccommodationQueryRepository;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationService {

    private final DateManager dateManager;
    private final AccommodationQueryRepository accommodationQueryRepository;

    public List<MainAccResDto> getAccommodations(Long guestId) {
        //TODO : 데이터 많아지면 네이티브 쿼리 고려
        LocalDate now = LocalDate.now();
        Season season = dateManager.getSeason(now);
        DayType dayType = dateManager.getDayType(now);

        List<MainAccListQueryDto> accommodations = accommodationQueryRepository.getAreaAccommodations(season, dayType, guestId);

        return accommodations
                .stream()
                .collect(groupingBy(
                        MainAccListQueryDto::getAreaKey,
                        collectingAndThen(
                                toList(),
                                dtos -> dtos.stream()
                                            .map(MainAccListResDto::from)
                                            .limit(8).toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> new MainAccResDto(
                        entry.getKey().areaName(),
                        entry.getKey().areaCode(),
                        entry.getValue())
                )
                .toList();
    }

    public PageResponseDto<FilteredAccListResDto> getFilteredPagingAccommodations(AccSearchCondDto searchDto, Long guestId, Pageable pageable) {
        LocalDate now = LocalDate.now();
        Season season = dateManager.getSeason(now);
        DayType dayType = dateManager.getDayType(now);

        Page<FilteredAccListResDto> result = accommodationQueryRepository.getFilteredPagingAccommodations(searchDto, guestId, pageable, season, dayType);

        return PageResponseDto.<FilteredAccListResDto>builder()
                              .contents(result.getContent())
                              .pageNumber(pageable.getPageNumber())
                              .pageSize(pageable.getPageSize())
                              .total(result.getTotalElements())
                              .build();
    }
}
