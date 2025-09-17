package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.consts.DayType;
import project.airbnb.clone.consts.Season;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.accommodation.AccSearchCondDto;
import project.airbnb.clone.repository.dto.DetailAccommodationQueryDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto.DetailImageDto;
import project.airbnb.clone.dto.accommodation.DetailAccommodationResDto.DetailReviewDto;
import project.airbnb.clone.dto.accommodation.FilteredAccListResDto;
import project.airbnb.clone.repository.dto.ImageDataQueryDto;
import project.airbnb.clone.repository.dto.MainAccListQueryDto;
import project.airbnb.clone.dto.accommodation.MainAccListResDto;
import project.airbnb.clone.dto.accommodation.MainAccResDto;
import project.airbnb.clone.repository.query.AccommodationQueryRepository;
import project.airbnb.clone.service.DateManager;

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

    public DetailAccommodationResDto getDetailAccommodation(Long accId, Long guestId) {
        LocalDate now = LocalDate.now();
        Season season = dateManager.getSeason(now);
        DayType dayType = dateManager.getDayType(now);

        DetailAccommodationQueryDto detailAccQueryDto = accommodationQueryRepository.findAccommodation(accId, guestId, season, dayType)
                                                                                    .orElseThrow(() -> new EntityNotFoundException("Cannot found accommodation from : " + accId));
        List<ImageDataQueryDto> images = accommodationQueryRepository.findImages(accId);
        List<String> amenities = accommodationQueryRepository.findAmenities(accId);
        List<DetailReviewDto> reviews = accommodationQueryRepository.findReviews(accId);

        String thumbnail = images.stream()
                                 .filter(ImageDataQueryDto::isThumbnail)
                                 .map(ImageDataQueryDto::imageUrl)
                                 .findFirst()
                                 .orElse(null);
        List<String> others = images.stream()
                                    .filter(dto -> !dto.isThumbnail())
                                    .map(ImageDataQueryDto::imageUrl)
                                    .toList();
        DetailImageDto detailImageDto = new DetailImageDto(thumbnail, others);

        return DetailAccommodationResDto.from(detailAccQueryDto, detailImageDto, amenities, reviews);
    }
}
