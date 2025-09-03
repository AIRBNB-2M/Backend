package project.airbnb.clone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.dto.AccommodationProcessorDto;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity
@NoArgsConstructor
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

    @Column(name = "description", length = 700)
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "max_people", nullable = true)
    private Integer maxPeople;

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
    
    @Column(name = "content_id", unique = true, nullable = false)
    private String contentId;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;

    @ManyToOne
    @JoinColumn(name = "signugu_code")
    private SigunguCode sigunguCode;
    
    public void setMapX(Double mapX) { this.mapX = mapX; }
    public void setMapY(Double mapY) { this.mapY = mapY; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setMaxPeople(Integer maxPeople) { this.maxPeople = maxPeople; }
    public void setMinPrice(Integer price) { this.minPrice = price; }
    public void setTitle(String title) { this.title = title; }
    public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
    public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    public void setNumber(String number) { this.number = number; }

    public void updateOrInit(AccommodationProcessorDto dto, SigunguCode sigunguCode) {
        this.mapX = dto.getMapX();
        this.mapY = dto.getMapY();
        this.description = dto.getDescription();
        this.address = dto.getAddress();
        this.maxPeople = dto.getMaxPeople();
        this.minPrice = dto.getMinPrice();
        this.maxPrice = dto.getMaxPrice();
        this.title = dto.getTitle();
        this.checkIn = dto.getCheckIn();
        this.checkOut = dto.getCheckOut();
        this.number = dto.getNumber();
        this.modifiedTime = dto.getModifiedTime();
        this.contentId = dto.getContentId();
        this.sigunguCode = sigunguCode;
    }
}