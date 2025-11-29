package project.airbnb.clone.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import project.airbnb.clone.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
