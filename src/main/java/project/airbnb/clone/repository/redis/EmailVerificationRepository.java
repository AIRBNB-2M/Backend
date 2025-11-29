package project.airbnb.clone.repository.redis;

import org.springframework.data.repository.CrudRepository;
import project.airbnb.clone.repository.dto.redis.EmailVerification;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
