package project.airbnb.clone.entity.area;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.airbnb.clone.entity.BaseEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", nullable = false)
    private AreaCode areaCode;

    public static SigunguCode create(String code, String codeName, AreaCode areaCode) {
        return new SigunguCode(code, codeName, areaCode);
    }

    private SigunguCode(String code, String codeName, AreaCode areaCode) {
        this.code = code;
        this.codeName = codeName;
        this.areaCode = areaCode;
    }
}