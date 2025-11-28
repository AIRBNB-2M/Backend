package project.airbnb.clone.service.accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.common.exceptions.factory.AccommodationExceptions;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
import project.airbnb.clone.common.exceptions.factory.WishlistExceptions;
import project.airbnb.clone.dto.wishlist.*;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;
import project.airbnb.clone.repository.dto.AccAllImagesQueryDto;
import project.airbnb.clone.repository.dto.WishlistDetailQueryDto;
import project.airbnb.clone.repository.jpa.AccommodationRepository;
import project.airbnb.clone.repository.jpa.MemberRepository;
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

    private final MemberRepository memberRepository;
    private final WishlistRepository wishlistRepository;
    private final AccommodationRepository accommodationRepository;
    private final WishlistQueryRepository wishlistQueryRepository;
    private final WishlistAccommodationRepository wishlistAccommodationRepository;

    @Transactional
    public WishlistCreateResDto createWishlist(WishlistCreateReqDto reqDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(memberId));

        Wishlist savedWishlist = wishlistRepository.save(Wishlist.create(member, reqDto.wishlistName()));
        return new WishlistCreateResDto(savedWishlist.getId(), savedWishlist.getName());
    }

    @Transactional
    public void addAccommodationToWishlist(Long wishlistId, AddAccToWishlistReqDto reqDto, Long memberId) {
        Long accommodationId = reqDto.accommodationId();
        if (wishlistQueryRepository.existsWishlistAccommodation(wishlistId, accommodationId, memberId)) {
            return;
        }
        Wishlist wishlist = getWishlistByIdAndMemberId(wishlistId, memberId);
        Accommodation accommodation = getAccommodationById(accommodationId);

        wishlistAccommodationRepository.save(WishlistAccommodation.create(wishlist, accommodation));
    }

    @Transactional
    public void removeAccommodationFromWishlist(Long wishlistId, Long accommodationId, Long memberId) {
        Wishlist wishlist = getWishlistByIdAndMemberId(wishlistId, memberId);
        Accommodation accommodation = getAccommodationById(accommodationId);

        wishlistAccommodationRepository.deleteByWishlistAndAccommodation(wishlist, accommodation);
    }

    @Transactional
    public void updateWishlistName(Long wishlistId, WishlistUpdateReqDto reqDto, Long memberId) {
        Wishlist wishlist = getWishlistByIdAndMemberId(wishlistId, memberId);
        wishlist.updateName(reqDto.wishlistName());
    }

    @Transactional
    public void removeWishlist(Long wishlistId, Long memberId) {
        Wishlist wishlist = getWishlistByIdAndMemberId(wishlistId, memberId);
        wishlistAccommodationRepository.deleteByWishlist(wishlist);
        wishlistRepository.delete(wishlist);
    }

    @Transactional
    public void updateMemo(Long wishlistId, Long accommodationId, Long memberId, MemoUpdateReqDto reqDto) {
        WishlistAccommodation wishlistAccommodation = wishlistAccommodationRepository.findByAllIds(wishlistId, accommodationId, memberId)
                                                                                     .orElseThrow(() -> WishlistExceptions.notFoundWishlistAccommodation(wishlistId, accommodationId, memberId));
        wishlistAccommodation.updateMemo(reqDto.memo());
    }

    public List<WishlistDetailResDto> getAccommodationsFromWishlist(Long wishlistId, Long memberId) {
        List<WishlistDetailQueryDto> detailQueryDtos = wishlistQueryRepository.findWishlistDetails(wishlistId, memberId);
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

    public List<WishlistsResDto> getAllWishlists(Long memberId) {
        return wishlistQueryRepository.getAllWishlists(memberId);
    }

    private Wishlist getWishlistByIdAndMemberId(Long wishlistId, Long memberId) {
        return wishlistRepository.findByIdAndMemberId(wishlistId, memberId).orElseThrow(
                () -> WishlistExceptions.notFoundByIdAndMemberId(wishlistId, memberId));
    }

    private Accommodation getAccommodationById(Long accommodationId) {
        return accommodationRepository.findById(accommodationId).orElseThrow(
                () -> AccommodationExceptions.notFoundById(accommodationId));
    }
}
