package project.airbnb.clone.repository.guest;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.entity.Guest;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    default Guest getGuestByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found Guest from: " + email
        ));
    }

    default Guest getGuestById(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found Guest from: " + id
        ));
    }

    Optional<Guest> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndSocialType(String email, SocialType socialType);
}