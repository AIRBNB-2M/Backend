package project.airbnb.clone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.airbnb.clone.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
	Optional<Payment> findByMerchantUid(String merchantUid);
    Optional<Payment> findByImpUid(String impUid);
}
