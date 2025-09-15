package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.MemoUpdateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistDetailResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;
import project.airbnb.clone.repository.dto.AccAllImagesQueryDto;
import project.airbnb.clone.repository.dto.WishlistDetailQueryDto;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.jpa.WishlistAccommodationRepository;
import project.airbnb.clone.repository.jpa.WishlistRepository;
import project.airbnb.clone.repository.query.WishlistQueryRepository;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final GuestRepository guestRepository;
    private final WishlistRepository wishlistRepository;
    private final AccommodationRepository accommodationRepository;
    private final WishlistQueryRepository wishlistQueryRepository;
    private final WishlistAccommodationRepository wishlistAccommodationRepository;

    @Transactional
    public WishlistCreateResDto createWishlist(WishlistCreateReqDto reqDto, Long guestId) {
        Guest guest = guestRepository.getGuestById(guestId);
        Wishlist savedWishlist = wishlistRepository.save(Wishlist.builder()
                                                                 .guest(guest)
                                                                 .name(reqDto.wishlistName())
                                                                 .build());
        return new WishlistCreateResDto(savedWishlist.getId(), savedWishlist.getName());
    }

    @Transactional
    public void addAccommodationToWishlist(Long wishlistId, AddAccToWishlistReqDto reqDto, Long guestId) {
        Long accommodationId = reqDto.accommodationId();
        if (wishlistQueryRepository.existsWishlistAccommodation(wishlistId, accommodationId, guestId)) {
            return;
        }
        Wishlist wishlist = getWishlistByIdAndGuestId(wishlistId, guestId);
        Accommodation accommodation = getAccommodationById(accommodationId);

        wishlistAccommodationRepository.save(WishlistAccommodation.builder()
                                                                  .wishlist(wishlist)
                                                                  .accommodation(accommodation)
                                                                  .build());
    }

    @Transactional
    public void removeAccommodationFromWishlist(Long wishlistId, Long accommodationId, Long guestId) {
        Wishlist wishlist = getWishlistByIdAndGuestId(wishlistId, guestId);
        Accommodation accommodation = getAccommodationById(accommodationId);

        wishlistAccommodationRepository.deleteByWishlistAndAccommodation(wishlist, accommodation);
    }

    @Transactional
    public void updateWishlistName(Long wishlistId, WishlistUpdateReqDto reqDto, Long guestId) {
        Wishlist wishlist = getWishlistByIdAndGuestId(wishlistId, guestId);
        wishlist.updateName(reqDto.wishlistName());
    }

    @Transactional
    public void removeWishlist(Long wishlistId, Long guestId) {
        Wishlist wishlist = getWishlistByIdAndGuestId(wishlistId, guestId);
        wishlistAccommodationRepository.deleteByWishlist(wishlist);
        wishlistRepository.delete(wishlist);
    }

    @Transactional
    public void updateMemo(Long wishlistId, Long accommodationId, Long guestId, MemoUpdateReqDto reqDto) {
        WishlistAccommodation wishlistAccommodation = wishlistAccommodationRepository.findByAllIds(wishlistId, accommodationId, guestId)
                                                                                     .orElseThrow(() -> new EntityNotFoundException("Cannot be found wishlistAccommodation for wishlistId: " + wishlistId + ", accommodationId: " + accommodationId + ", guestId: " + guestId));
        wishlistAccommodation.updateMemo(reqDto.memo());
    }

    public List<WishlistDetailResDto> getAccommodationsFromWishlist(Long wishlistId, Long guestId) {
        List<WishlistDetailQueryDto> detailQueryDtos = wishlistQueryRepository.findWishlistDetails(wishlistId, guestId);
        List<Long> accIds = detailQueryDtos.stream()
                                           .map(WishlistDetailQueryDto::accommodationId)
                                           .toList();

        List<AccAllImagesQueryDto> allImagesQueryDtos = wishlistQueryRepository.findAllImages(accIds);
        Map<Long, List<String>> imagesMap = allImagesQueryDtos.stream()
                                                              .collect(groupingBy(
                                                                      AccAllImagesQueryDto::accommodationId,
                                                                      mapping(AccAllImagesQueryDto::imageUrl, toList())
                                                              ));

        return detailQueryDtos.stream()
                              .map(dto -> WishlistDetailResDto.from(dto, imagesMap.getOrDefault(dto.accommodationId(), List.of())))
                              .toList();
    }

    private Wishlist getWishlistByIdAndGuestId(Long wishlistId, Long guestId) {
        return wishlistRepository.findByIdAndGuestId(wishlistId, guestId).orElseThrow(
                () -> new EntityNotFoundException("Cannot be found wishlist for wishlistId: " + wishlistId + ", guestId: " + guestId));
    }

    private Accommodation getAccommodationById(Long accommodationId) {
        return accommodationRepository.findById(accommodationId).orElseThrow(
                () -> new EntityNotFoundException("Cannot found accommodation for : " + accommodationId));
    }
}
