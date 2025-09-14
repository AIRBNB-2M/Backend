package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.airbnb.clone.TestContainersConfig;
import project.airbnb.clone.dto.wishlist.AddAccToWishlistReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.dto.wishlist.WishlistUpdateReqDto;
import project.airbnb.clone.entity.Accommodation;
import project.airbnb.clone.entity.AreaCode;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.SigunguCode;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.entity.WishlistAccommodation;
import project.airbnb.clone.repository.jpa.WishlistAccommodationRepository;
import project.airbnb.clone.repository.jpa.WishlistRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistServiceTest extends TestContainersConfig {

    @Autowired EntityManager em;
    @Autowired WishlistService wishlistService;
    @Autowired WishlistRepository wishlistRepository;
    @Autowired WishlistAccommodationRepository wishlistAccommodationRepository;

    Guest guest;

    @BeforeEach
    void setUp() {
        Guest guest = Guest.builder()
                            .name("test-user")
                            .email(UUID.randomUUID() + "@test.com")
                            .password(UUID.randomUUID().toString())
                            .build();
        em.persist(guest);
        this.guest = guest;
    }

    @Test
    @DisplayName("위시리시트 요청 DTO를 받아서 Wishlist 데이터를 저장한다.")
    void createWishlist() {
        //given
        WishlistCreateReqDto reqDto = new WishlistCreateReqDto("my-wishlist");

        //when
        WishlistCreateResDto resDto = wishlistService.createWishlist(reqDto, guest.getId());

        em.flush();
        em.clear();

        //then
        Wishlist wishlist = wishlistRepository.findById(resDto.wishlistId()).get();
        assertThat(wishlist).isNotNull();
        assertThat(resDto.wishlistId()).isEqualTo(wishlist.getId());
        assertThat(resDto.wishlistName()).isEqualTo(wishlist.getName());
        assertThat(wishlist.getGuest().getId()).isEqualTo(guest.getId());
    }

    @Test
    @DisplayName("위시리스트에 숙소를 추가한다.")
    void addAccommodationToWishlist() {
        //given
        Wishlist wishlist = savedAndGetWishlist();
        Accommodation accommodation = saveAndGetAccommodation();
        AddAccToWishlistReqDto reqDto = new AddAccToWishlistReqDto(accommodation.getId());

        //when
        wishlistService.addAccommodationToWishlist(wishlist.getId(), reqDto, guest.getId());
        em.flush();
        em.clear();

        //then
        List<WishlistAccommodation> savedList = wishlistAccommodationRepository.findAll();

        assertThat(savedList).hasSize(1);

        WishlistAccommodation result = savedList.get(0);
        assertThat(result.getWishlist().getId()).isEqualTo(wishlist.getId());
        assertThat(result.getAccommodation().getId()).isEqualTo(accommodation.getId());
    }

    @Test
    @DisplayName("위시리스트에서 숙소를 제거한다.")
    void removeAccommodationFromWishlist() {
        //given
        Wishlist wishlist = savedAndGetWishlist();
        Accommodation accommodation = saveAndGetAccommodation();

        //when
        wishlistService.removeAccommodationFromWishlist(wishlist.getId(), accommodation.getId(), guest.getId());
        em.flush();
        em.clear();

        //then
        List<WishlistAccommodation> result = em.createQuery("SELECT wa FROM WishlistAccommodation wa WHERE wa.accommodation.id = :accommodationId AND wa.wishlist.id = :wishlistId", WishlistAccommodation.class)
                                               .setParameter("accommodationId", accommodation.getId())
                                               .setParameter("wishlistId", wishlist.getId())
                                               .getResultList();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("위시리스트의 이름을 변경한다.")
    void updateWishlistName() {
        //given
        Wishlist wishlist = savedAndGetWishlist();
        WishlistUpdateReqDto reqDto = new WishlistUpdateReqDto("test-new-wishlist-name");

        //when
        wishlistService.updateWishlistName(wishlist.getId(), reqDto, guest.getId());
        em.flush();
        em.clear();

        //then
        Wishlist result = wishlistRepository.findById(wishlist.getId()).get();
        assertThat(result.getName()).isEqualTo(reqDto.wishlistName());
    }

    @Test
    @DisplayName("특정 위시리스트를 삭제한다.")
    void removeWishlist() {
        //given
        Wishlist wishlist = savedAndGetWishlist();
        Accommodation acc1 = saveAndGetAccommodation();
        Accommodation acc2 = saveAndGetAccommodation();

        WishlistAccommodation wa1 = saveAndGetWishlistAccommodation(wishlist, acc1);
        WishlistAccommodation wa2 = saveAndGetWishlistAccommodation(wishlist, acc2);

        //when
        wishlistService.removeWishlist(wishlist.getId(), guest.getId());
        em.flush();
        em.clear();

        //then
        WishlistAccommodation waResult1 = wishlistAccommodationRepository.findById(wa1.getId()).orElse(null);
        WishlistAccommodation waResult2 = wishlistAccommodationRepository.findById(wa2.getId()).orElse(null);
        assertThat(waResult1).isNull();
        assertThat(waResult2).isNull();

        Wishlist result = wishlistRepository.findById(wishlist.getId()).orElse(null);
        assertThat(result).isNull();
    }

    private WishlistAccommodation saveAndGetWishlistAccommodation(Wishlist wishlist, Accommodation acc) {
        return wishlistAccommodationRepository.save(WishlistAccommodation.builder()
                                                                  .wishlist(wishlist)
                                                                  .accommodation(acc)
                                                                  .build());
    }

    private Wishlist savedAndGetWishlist() {
        return wishlistRepository.save(Wishlist.builder()
                                               .guest(guest)
                                               .name("test-wishlist")
                                               .build());
    }

    private Accommodation saveAndGetAccommodation() {
        AreaCode areaCode = new AreaCode(UUID.randomUUID().toString(), "test-codeName");
        SigunguCode sigunguCode = new SigunguCode(UUID.randomUUID().toString(), "test-codeName", areaCode);
        em.persist(areaCode);
        em.persist(sigunguCode);

        Accommodation accommodation = Accommodation.builder()
                                                   .mapX(1.0)
                                                   .mapY(1.0)
                                                   .title("test-title")
                                                   .address("test-address")
                                                   .contentId(UUID.randomUUID().toString())
                                                   .modifiedTime(LocalDateTime.now())
                                                   .sigunguCode(sigunguCode)
                                                   .build();
        em.persist(accommodation);
        return accommodation;
    }
}