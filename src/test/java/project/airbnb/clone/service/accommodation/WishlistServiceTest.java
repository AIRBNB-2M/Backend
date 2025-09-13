package project.airbnb.clone.service.accommodation;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.airbnb.clone.TestContainersConfig;
import project.airbnb.clone.dto.wishlist.WishlistCreateReqDto;
import project.airbnb.clone.dto.wishlist.WishlistCreateResDto;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.entity.Wishlist;
import project.airbnb.clone.repository.jpa.WishlistRepository;
import project.airbnb.clone.repository.jpa.GuestRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistServiceTest extends TestContainersConfig {

    @Autowired
    GuestRepository guestRepository;
    @Autowired
    WishlistRepository wishlistRepository;
    @Autowired
    WishlistService wishlistService;
    @Autowired
    EntityManager em;

    Guest guest;

    @BeforeEach
    void setUp() {
        this.guest = guestRepository.save(Guest.builder()
                                               .name("test-user")
                                               .email(UUID.randomUUID() + "@test.com")
                                               .password(UUID.randomUUID().toString())
                                               .build());
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
}