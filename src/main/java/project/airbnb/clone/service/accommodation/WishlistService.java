package project.airbnb.clone.service.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.repository.jpa.WishlistRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final GuestRepository guestRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public WishlistCreateResDto createWishlist(WishlistCreateReqDto reqDto, Long guestId) {
        Guest guest = guestRepository.getGuestById(guestId);
        Wishlist savedWishlist = wishlistRepository.save(Wishlist.builder()
                                                                 .guest(guest)
                                                                 .name(reqDto.wishlistName())
                                                                 .build());
        return new WishlistCreateResDto(savedWishlist.getId(), savedWishlist.getName());
    }
}
