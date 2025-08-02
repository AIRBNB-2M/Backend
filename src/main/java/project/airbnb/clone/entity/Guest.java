package project.airbnb.clone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guests")
public class Guest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "number", nullable = false, length = 11)
    private String number;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "profile_url", nullable = false)
    private String profileUrl;

    @Column(name = "about_me")
    private String aboutMe;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}