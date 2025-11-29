package project.airbnb.clone.repository.redis;

import org.springframework.data.repository.CrudRepository;
import project.airbnb.clone.repository.dto.redis.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
