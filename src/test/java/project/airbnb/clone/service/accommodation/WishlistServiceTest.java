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

    private Wishlist savedAndGetWishlist() {
        return wishlistRepository.save(Wishlist.builder()
                                               .guest(guest)
                                               .name("test-wishlist")
                                               .build());
    }

    private Accommodation saveAndGetAccommodation() {
        AreaCode areaCode = new AreaCode("test-code", "test-codeName");
        SigunguCode sigunguCode = new SigunguCode("test-code", "test-codeName", areaCode);
        em.persist(areaCode);
        em.persist(sigunguCode);

        Accommodation accommodation = Accommodation.builder()
                                                   .mapX(1.0)
                                                   .mapY(1.0)
                                                   .title("test-title")
                                                   .address("test-address")
                                                   .contentId("test-contentId")
                                                   .modifiedTime(LocalDateTime.now())
                                                   .sigunguCode(sigunguCode)
                                                   .build();
        em.persist(accommodation);

        return em.find(Accommodation.class, accommodation.getId());
    }
}