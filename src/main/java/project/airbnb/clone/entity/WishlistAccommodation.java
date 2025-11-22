package project.airbnb.clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wishlist_accommodations", uniqueConstraints =
    @UniqueConstraint(name = "uk_wishlist_accommodation_wid_aid", columnNames = {"wishlist_id" , "accommodation_id"})
)
public class WishlistAccommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_accommodation_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(name = "memo", length = 250)
    private String memo;

    public static WishlistAccommodation create(Wishlist wishlist, Accommodation accommodation) {
        return new WishlistAccommodation(wishlist, accommodation);
    }

    private WishlistAccommodation(Wishlist wishlist, Accommodation accommodation) {
        this.wishlist = wishlist;
        this.accommodation = accommodation;
    }

    public void updateMemo(String newMemo) {
        this.memo = newMemo;
    }
}