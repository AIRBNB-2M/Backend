package project.airbnb.clone.repository.redis;

import org.springframework.data.repository.CrudRepository;
import project.airbnb.clone.repository.dto.redis.TempPayment;

public interface TempPaymentRepository extends CrudRepository<TempPayment, String> {
}
