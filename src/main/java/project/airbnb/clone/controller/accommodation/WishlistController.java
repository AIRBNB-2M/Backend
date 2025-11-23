package project.airbnb.clone.controller.accommodation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.wishlist.*;
import project.airbnb.clone.service.accommodation.WishlistService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<WishlistCreateResDto> createWishlist(@Valid @RequestBody WishlistCreateReqDto reqDto,
                                                               @CurrentMemberId Long memberId) {
        WishlistCreateResDto resDto = wishlistService.createWishlist(reqDto, memberId);
        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    @PostMapping("/{wishlistId}/accommodations")
    public ResponseEntity<?> addAccommodation(@PathVariable("wishlistId") Long wishlistId,
                                              @RequestBody AddAccToWishlistReqDto reqDto,
                                              @CurrentMemberId Long memberId) {
        wishlistService.addAccommodationToWishlist(wishlistId, reqDto, memberId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{wishlistId}/accommodations/{accommodationId}")
    public ResponseEntity<?> updateMemo(@PathVariable("wishlistId") Long wishlistId,
                                        @PathVariable("accommodationId") Long accommodationId,
                                        @Valid @RequestBody MemoUpdateReqDto reqDto,
                                        @CurrentMemberId Long memberId) {
        wishlistService.updateMemo(wishlistId, accommodationId, memberId, reqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{wishlistId}/accommodations/{accommodationId}")
    public ResponseEntity<?> removeAccommodation(@PathVariable("wishlistId") Long wishlistId,
                                                 @PathVariable("accommodationId") Long accommodationId,
                                                 @CurrentMemberId Long memberId) {
        wishlistService.removeAccommodationFromWishlist(wishlistId, accommodationId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<?> updateWishlistName(@PathVariable("wishlistId") Long wishlistId,
                                                @Valid @RequestBody WishlistUpdateReqDto reqDto,
                                                @CurrentMemberId Long memberId) {
        wishlistService.updateWishlistName(wishlistId, reqDto, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> removeWishlist(@PathVariable("wishlistId") Long wishlistId,
                                            @CurrentMemberId Long memberId) {
        wishlistService.removeWishlist(wishlistId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{wishlistId}")
    public ResponseEntity<List<WishlistDetailResDto>> getAccommodationsFromWishlist(@PathVariable("wishlistId") Long wishlistId,
                                                                                    @CurrentMemberId Long memberId) {
        List<WishlistDetailResDto> result = wishlistService.getAccommodationsFromWishlist(wishlistId, memberId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<WishlistsResDto>> getAllWishlists(@CurrentMemberId Long memberId) {
        List<WishlistsResDto> result = wishlistService.getAllWishlists(memberId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
