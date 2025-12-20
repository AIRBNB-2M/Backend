package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
