package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.jpa.WishlistAccommodationRepository;
import project.airbnb.clone.repository.jpa.WishlistRepository;
import project.airbnb.clone.repository.query.WishlistQueryRepository;

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

    private Wishlist getWishlistByIdAndGuestId(Long wishlistId, Long guestId) {
        return wishlistRepository.findByIdAndGuestId(wishlistId, guestId).orElseThrow(
                () -> new EntityNotFoundException("Cannot be found wishlist for wishlistId: " + wishlistId + ", guestId: " + guestId));
    }

    private Accommodation getAccommodationById(Long accommodationId) {
        return accommodationRepository.findById(accommodationId).orElseThrow(
                () -> new EntityNotFoundException("Cannot found accommodation for : " + accommodationId));
    }
}
