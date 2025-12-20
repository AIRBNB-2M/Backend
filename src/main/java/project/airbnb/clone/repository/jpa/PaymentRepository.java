package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.reservation.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
