package project.airbnb.clone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "accommodations")
public class Accommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_id", nullable = false)
    private Long id;

    @Column(name = "map_x", nullable = false)
    private Double mapX;

    @Column(name = "map_y", nullable = false)
    private Double mapY;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "max_people", nullable = true)
    private Short maxPeople;

    @Column(name = "min_price", nullable = true)
    private Integer minPrice;

    @Column(name = "max_price", nullable = true)
    private Integer maxPrice;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "check_in", nullable = true)
    private String checkIn;

    @Column(name = "check_out", nullable = true)
    private String checkOut;

    @Column(name = "number", nullable = true, length = 50)
    private String number;
    
    @Column(name = "content_id", nullable = false, length = 30)
    private String contentId;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;

    @OneToOne
    @JoinColumn(name = "signugu_code")
    private SigunguCode sigunguCode;
    
    public void setMapX(Double mapX) { this.mapX = mapX; }
    public void setMapY(Double mapY) { this.mapY = mapY; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setMaxPeople(Short maxPeople) { this.maxPeople = maxPeople; }
    public void setMinPrice(Integer price) { this.minPrice = price; }
    public void setTitle(String title) { this.title = title; }
    public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
    public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    public void setNumber(String number) { this.number = number; }
}