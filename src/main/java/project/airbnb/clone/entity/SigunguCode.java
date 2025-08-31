package project.airbnb.clone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sigungu_codes")
public class SigunguCode extends BaseEntity {

    @Id
    @Column(name = "sigungu_code", nullable = false)
    private String code;

    @Column(name = "code_name", nullable = false)
    private String codeName;

    @ManyToOne
    @JoinColumn(name = "area_code", nullable = false)
    private AreaCode areaCode;
}