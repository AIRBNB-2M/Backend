package project.airbnb.clone.repository.redis;

import org.springframework.data.repository.CrudRepository;
import project.airbnb.clone.repository.dto.BlacklistedToken;

public interface BlacklistedTokenRepository extends CrudRepository<BlacklistedToken, String> {
}
