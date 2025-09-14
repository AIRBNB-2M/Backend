package project.airbnb.clone.controller.accommodation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.service.accommodation.WishlistService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<WishlistCreateResDto> createWishlist(@Valid @RequestBody WishlistCreateReqDto reqDto,
                                                               @CurrentGuestId Long guestId) {
        WishlistCreateResDto resDto = wishlistService.createWishlist(reqDto, guestId);
        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    @PostMapping("/{wishlistId}/accommodations")
    public ResponseEntity<?> addAccommodation(@PathVariable("wishlistId") Long wishlistId,
                                              @RequestBody AddAccToWishlistReqDto reqDto,
                                              @CurrentGuestId Long guestId) {
        wishlistService.addAccommodationToWishlist(wishlistId, reqDto, guestId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{wishlistId}/accommodations/{accommodationId}")
    public ResponseEntity<?> removeAccommodation(@PathVariable("wishlistId") Long wishlistId,
                                                 @PathVariable("accommodationId") Long accommodationId,
                                                 @CurrentGuestId Long guestId) {
        wishlistService.removeAccommodationFromWishlist(wishlistId, accommodationId, guestId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<?> updateWishlistName(@PathVariable("wishlistId") Long wishlistId,
                                                @Valid @RequestBody WishlistUpdateReqDto reqDto,
                                                @CurrentGuestId Long guestId) {
        wishlistService.updateWishlistName(wishlistId, reqDto, guestId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
