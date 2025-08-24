package project.airbnb.clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
