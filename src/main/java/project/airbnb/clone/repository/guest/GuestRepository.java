package project.airbnb.clone.repository.guest;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.Guest;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByEmail(String email);
    boolean existsByEmail(String email);
}